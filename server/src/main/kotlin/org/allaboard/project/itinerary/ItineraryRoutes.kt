package org.allaboard.project.itinerary

import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.patch
import io.ktor.serialization.kotlinx.json.json
import org.allaboard.project.SupabaseConfig
import org.allaboard.project.auth.userId
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.Itinerary
import org.allaboard.project.domain.ItineraryDay
import org.allaboard.project.domain.ScheduledActivity
import org.allaboard.project.domain.TravelVibe
import org.allaboard.project.domain.Vote
import org.allaboard.project.domain.VoteType
import org.allaboard.project.trip.fetchTripWithMembers
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

private enum class DaySlot {
    MORNING,
    LUNCH,
    AFTERNOON,
    DINNER,
    NIGHT
}

private val logger = LoggerFactory.getLogger("ItineraryRoutes")
private val tripGenerationLocks = ConcurrentHashMap<String, Mutex>()
private val googleCalendarHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

private data class ActivityCandidate(
    val activity: Activity,
    val voteScore: Int,
    val durationMinutes: Int,
    val allowedSlots: Set<DaySlot>,
    val typeBucket: String
)

private data class PlannedActivity(
    val activity: Activity,
    val startMinute: Int,
    val endMinute: Int
)

private data class GenerationTuning(
    val maxActivitiesPerDay: Int,
    val travelPenaltyWeight: Double,
    val bufferMinutes: Int,
    val dayEndMinute: Int
)

private data class ReservedMeal(
    val candidate: ActivityCandidate,
    val slot: DaySlot
)

private data class ReservedAfternoonActivity(
    val candidate: ActivityCandidate
)

private data class DayPlanContext(
    var currentMinute: Int,
    var lastActivity: Activity?,
    var lastTypeBucket: String?,
    var nonFoodCount: Int,
    val bucketCount: MutableMap<String, Int>,
    val usedFoodSlots: MutableSet<DaySlot>,
    val coveredNonFoodSlots: MutableSet<DaySlot>,
    val plan: MutableList<PlannedActivity>
)

private val DISPLAY_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private const val START_TIME_RANDOM_STEP_MINUTES = 5
private const val START_TIME_RANDOM_MAX_OFFSET_MINUTES = 30
private const val VOTE_WEIGHT_MULTIPLIER = 7.0

private val GOOGLE_RFC3339_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
private val DISPLAY_TIME_PARSERS: List<DateTimeFormatter> = listOf(
    DateTimeFormatter.ofPattern("h:mm a", Locale.US),
    DateTimeFormatter.ofPattern("h:mma", Locale.US),
    DateTimeFormatter.ofPattern("h a", Locale.US),
    DateTimeFormatter.ofPattern("ha", Locale.US),
    DateTimeFormatter.ofPattern("H:mm:ss", Locale.US),
    DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US),
    DateTimeFormatter.ofPattern("H:mm", Locale.US),
    DateTimeFormatter.ofPattern("HH:mm", Locale.US)
)

@Serializable
private data class GoogleCalendarEventDateTime(
    val dateTime: String,
    val timeZone: String
)

@Serializable
private data class GoogleCalendarEventInsertRequest(
    val summary: String,
    val location: String? = null,
    val description: String? = null,
    val start: GoogleCalendarEventDateTime,
    val end: GoogleCalendarEventDateTime
)

private fun parseIsoDateOrNull(raw: String): LocalDate? =
    runCatching { LocalDate.parse(raw) }.getOrNull()

private fun parseDisplayTimeOrNull(raw: String): LocalTime? {
    val text = raw.trim()
    if (text.isEmpty()) return null

    val normalized = text
        .replace(Regex("\\s+"), " ")
        .replace(Regex("(?i)\\b(am|pm)\\b")) { it.value.uppercase(Locale.US) }

    DISPLAY_TIME_PARSERS.forEach { formatter ->
        runCatching { LocalTime.parse(normalized, formatter) }
            .getOrNull()
            ?.let { return it }
    }
    return null
}

private fun toGoogleRfc3339(
    date: String,
    time: String,
    timeZone: String
): String? {
    val localDate = parseIsoDateOrNull(date) ?: return null
    val localTime = parseDisplayTimeOrNull(time) ?: return null
    val zoneId = runCatching { ZoneId.of(timeZone) }.getOrElse { ZoneId.of("UTC") }
    val zoned = LocalDateTime.of(localDate, localTime).atZone(zoneId)
    return GOOGLE_RFC3339_FORMATTER.format(zoned)
}

private fun datesBetweenInclusive(start: LocalDate, end: LocalDate): List<LocalDate> {
    if (end.isBefore(start)) return listOf(start)
    val days = mutableListOf<LocalDate>()
    var cursor = start
    while (!cursor.isAfter(end)) {
        days += cursor
        cursor = cursor.plusDays(1)
    }
    return days
}

private fun travelVibeScore(vibe: TravelVibe): Int = when (vibe) {
    TravelVibe.RELAXED -> 0
    TravelVibe.BALANCED -> 1
    TravelVibe.ADVENTUROUS -> 2
}

private fun tuneForGroupVibe(vibes: List<TravelVibe>): GenerationTuning {
    val average = if (vibes.isEmpty()) 1.0 else vibes.map(::travelVibeScore).average()
    return when {
        average < 0.75 -> GenerationTuning(
            maxActivitiesPerDay = 3,
            travelPenaltyWeight = 0.45,
            bufferMinutes = 20,
            dayEndMinute = 21 * 60
        )
        average < 1.5 -> GenerationTuning(
            maxActivitiesPerDay = 4,
            travelPenaltyWeight = 0.30,
            bufferMinutes = 15,
            dayEndMinute = 23 * 60
        )
        else -> GenerationTuning(
            maxActivitiesPerDay = 5,
            travelPenaltyWeight = 0.20,
            bufferMinutes = 10,
            dayEndMinute = 24 * 60
        )
    }
}

private fun isNightlife(activity: Activity): Boolean {
    val preference = activity.preference?.trim()?.lowercase() ?: ""
    return preference == "nightlife" ||
            preference == "night life" ||
            preference.contains("nightlife")
}

private fun isFoodActivity(activity: Activity): Boolean {
    if (activity.type.name == "RESTAURANT") return true
    val preference = activity.preference?.trim()?.lowercase() ?: return false
    return preference == "food" || preference == "restaurant" || preference == "dining"
}

private fun activityDurationMinutes(activity: Activity): Int {
    if (isNightlife(activity)) return 60
    return when (activity.type.name) {
        "RESTAURANT" -> 90
        "EXPERIENCES" -> 120
        "LANDMARK" -> 75
        else -> 90
    }
}

private fun allowedSlots(activity: Activity): Set<DaySlot> {
    if (isNightlife(activity)) return setOf(DaySlot.NIGHT)
    if (isFoodActivity(activity)) return setOf(DaySlot.LUNCH, DaySlot.DINNER)
    return setOf(DaySlot.MORNING, DaySlot.AFTERNOON)
}

private fun slotRanges(): Map<DaySlot, IntRange> = mapOf(
    DaySlot.MORNING to (8 * 60 until 11 * 60 + 30),
    DaySlot.LUNCH to (11 * 60 + 30 until 14 * 60),
    DaySlot.AFTERNOON to (14 * 60 until 18 * 60),
    DaySlot.DINNER to (18 * 60 until 20 * 60),
    DaySlot.NIGHT to (20 * 60 until 24 * 60)
)

private fun slotForMinute(minute: Int): DaySlot? {
    return slotRanges().entries.firstOrNull { (_, range) -> minute in range }?.key
}

private fun nextSlotBoundary(currentMinute: Int): Int? {
    val starts = slotRanges().values.map { it.first }.sorted()
    return starts.firstOrNull { it > currentMinute }
}

private fun formatDisplayTime(minuteOfDay: Int): String {
    val bounded = minuteOfDay.coerceIn(0, 23 * 60 + 59)
    val time = LocalTime.of(bounded / 60, bounded % 60)
    return time.format(DISPLAY_TIME_FORMATTER)
}

private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val radiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val originLat = Math.toRadians(lat1)
    val destLat = Math.toRadians(lat2)
    val a = sin(dLat / 2).pow(2.0) +
            cos(originLat) * cos(destLat) * sin(dLon / 2).pow(2.0)
    return 2 * radiusKm * asin(sqrt(a))
}

private fun estimateTravelMinutes(from: Activity?, to: Activity): Int {
    if (from == null) return 0
    val fromLat = from.latitude
    val fromLon = from.longitude
    val toLat = to.latitude
    val toLon = to.longitude

    if (fromLat != null && fromLon != null && toLat != null && toLon != null) {
        val km = haversineKm(fromLat, fromLon, toLat, toLon)
        val speedKmPerHour = 25.0
        return maxOf(5, ((km / speedKmPerHour) * 60).toInt())
    }

    if (from.location.equals(to.location, ignoreCase = true)) return 10
    return 25
}

private fun voteWeight(voteType: VoteType): Int = when (voteType) {
    VoteType.SUPER -> 3
    VoteType.YES -> 1
    VoteType.NO -> -2
}

private fun typeBucket(activity: Activity): String {
    return when {
        isNightlife(activity) -> "nightlife"
        isFoodActivity(activity) -> "food"
        else -> activity.type.name.lowercase()
    }
}

private fun buildCandidates(
    activities: List<Activity>,
    votes: List<Vote>
): MutableList<ActivityCandidate> {
    val scoreByActivity = votes.groupBy { it.activityId }
        .mapValues { (_, groupedVotes) -> groupedVotes.sumOf { voteWeight(it.voteType) } }

    return activities.map { activity ->
        ActivityCandidate(
            activity = activity,
            voteScore = scoreByActivity[activity.id] ?: 0,
            durationMinutes = activityDurationMinutes(activity),
            allowedSlots = allowedSlots(activity),
            typeBucket = typeBucket(activity)
        )
    }.toMutableList()
}

private fun firstValidStartMinute(
    allowed: Set<DaySlot>,
    earliestMinute: Int,
    durationMinutes: Int,
    latestAllowedEndMinute: Int
): Int? {
    val ranges = slotRanges()

    return allowed
        .mapNotNull { slot ->
            val range = ranges[slot] ?: return@mapNotNull null
            val earliestStart = maxOf(earliestMinute, range.first)
            val latestStart = minOf(range.last, latestAllowedEndMinute - durationMinutes)
            if (earliestStart > latestStart) return@mapNotNull null

            randomizedStartMinute(earliestStart, latestStart)
        }
        .minOrNull()
}

private fun randomizedStartMinute(earliestStart: Int, latestStart: Int): Int {
    if (earliestStart >= latestStart) return earliestStart
    val randomizedUpperBound = minOf(
        latestStart,
        earliestStart + START_TIME_RANDOM_MAX_OFFSET_MINUTES
    )
    val candidateWindow = randomizedUpperBound - earliestStart
    val steps = (candidateWindow / START_TIME_RANDOM_STEP_MINUTES) + 1
    val randomStep = if (steps > 1) Random.nextInt(steps) else 0
    return earliestStart + (randomStep * START_TIME_RANDOM_STEP_MINUTES)
}

private fun candidateStartMinute(
    candidate: ActivityCandidate,
    earliestMinute: Int,
    latestAllowedEndMinute: Int
): Int? {
    return firstValidStartMinute(
        allowed = candidate.allowedSlots,
        earliestMinute = earliestMinute,
        durationMinutes = candidate.durationMinutes,
        latestAllowedEndMinute = latestAllowedEndMinute
    )
}

private fun canAddBucket(bucket: String, bucketCount: Map<String, Int>): Boolean {
    val existing = bucketCount[bucket] ?: 0
    return when (bucket) {
        "food" -> existing < 2
        "nightlife" -> existing < 1
        else -> true
    }
}

private fun reserveRestaurantForDay(
    candidates: List<ActivityCandidate>,
    restaurantTarget: Int
): ReservedMeal? {
    if (restaurantTarget <= 0) return null

    fun bestMealForSlot(slot: DaySlot): ReservedMeal? {
        var best: ReservedMeal? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for (candidate in candidates) {
            if (candidate.typeBucket != "food") continue
            if (slot !in candidate.allowedSlots) continue

            val score = candidate.voteScore * VOTE_WEIGHT_MULTIPLIER
            if (score > bestScore) {
                bestScore = score
                best = ReservedMeal(
                    candidate = candidate,
                    slot = slot
                )
            }
        }

        return best
    }

    // Prefer dinner when available; lunch is fallback.
    return bestMealForSlot(DaySlot.DINNER) ?: bestMealForSlot(DaySlot.LUNCH)
}

private fun computeReservedMealPlacement(
    reservedMeal: ReservedMeal,
    context: DayPlanContext,
    tuning: GenerationTuning
): Pair<Int, Int>? {
    return computeReservedPlacement(
        slot = reservedMeal.slot,
        durationMinutes = reservedMeal.candidate.durationMinutes,
        context = context,
        latestAllowedEndMinute = tuning.dayEndMinute
    )
}

private fun reserveAfternoonNonFoodForDay(
    candidates: List<ActivityCandidate>
): ReservedAfternoonActivity? {
    var best: ReservedAfternoonActivity? = null
    var bestScore = Double.NEGATIVE_INFINITY

    for (candidate in candidates) {
        if (candidate.typeBucket == "food" || candidate.typeBucket == "nightlife") continue
        if (DaySlot.AFTERNOON !in candidate.allowedSlots) continue

        val score = (candidate.voteScore * VOTE_WEIGHT_MULTIPLIER) + 0.25
        if (score > bestScore) {
            bestScore = score
            best = ReservedAfternoonActivity(candidate = candidate)
        }
    }

    return best
}

private fun computeReservedPlacement(
    slot: DaySlot,
    durationMinutes: Int,
    context: DayPlanContext,
    latestAllowedEndMinute: Int
): Pair<Int, Int>? {
    val startMinute = firstValidStartMinute(
        allowed = setOf(slot),
        earliestMinute = context.currentMinute,
        durationMinutes = durationMinutes,
        latestAllowedEndMinute = latestAllowedEndMinute
    ) ?: return null

    val endMinute = startMinute + durationMinutes
    if (endMinute > latestAllowedEndMinute) return null

    return startMinute to endMinute
}

private fun computeReservedAfternoonPlacement(
    reservedAfternoon: ReservedAfternoonActivity,
    context: DayPlanContext,
    tuning: GenerationTuning
): Pair<Int, Int>? {
    val range = slotRanges()[DaySlot.AFTERNOON] ?: return null
    val earliestStart = maxOf(context.currentMinute, range.first)
    val latestStart = minOf(
        range.last,
        minOf(
            tuning.dayEndMinute - reservedAfternoon.candidate.durationMinutes,
            (range.last + 1) - reservedAfternoon.candidate.durationMinutes
        )
    )
    if (earliestStart > latestStart) return null
    val startMinute = randomizedStartMinute(earliestStart, latestStart)

    val endMinute = startMinute + reservedAfternoon.candidate.durationMinutes
    if (endMinute > (range.last + 1)) return null
    if (endMinute > tuning.dayEndMinute) return null

    return startMinute to endMinute
}

private fun chooseBestCandidateForWindow(
    candidates: List<ActivityCandidate>,
    tuning: GenerationTuning,
    context: DayPlanContext,
    windowEndMinute: Int,
    deferredActivityIds: Set<String>
): Triple<ActivityCandidate, Int, Int>? {
    val preferredNonFoodSlots = setOf(DaySlot.MORNING, DaySlot.AFTERNOON)

    var bestCandidate: ActivityCandidate? = null
    var bestStartMinute = 0
    var bestEndMinute = 0
    var bestScore = Double.NEGATIVE_INFINITY

    for (candidate in candidates) {
        if (!canAddBucket(candidate.typeBucket, context.bucketCount)) continue
        if (candidate.activity.id in deferredActivityIds) continue

        val travelMinutes = estimateTravelMinutes(context.lastActivity, candidate.activity)
        val earliest = context.currentMinute + travelMinutes
        val startMinute = candidateStartMinute(
            candidate = candidate,
            earliestMinute = earliest,
            latestAllowedEndMinute = minOf(windowEndMinute, tuning.dayEndMinute)
        ) ?: continue

        val endMinute = startMinute + candidate.durationMinutes
        if (endMinute > windowEndMinute) continue
        if (endMinute > tuning.dayEndMinute) continue

        val slot = slotForMinute(startMinute) ?: continue
        if (candidate.typeBucket == "food" && slot in context.usedFoodSlots) continue

        val existingBucketCount = context.bucketCount[candidate.typeBucket] ?: 0
        val repeatPenalty = if (candidate.typeBucket == context.lastTypeBucket) 2.5 else 0.0
        val diversityPenalty = existingBucketCount * 1.5

        val needsNonFood = context.nonFoodCount == 0 &&
                candidates.any { it.typeBucket != "food" && it.typeBucket != "nightlife" }

        val nonFoodBonus =
            if (needsNonFood && candidate.typeBucket != "food" && candidate.typeBucket != "nightlife") 2.0 else 0.0

        val slotCoverageBonus =
            if (candidate.typeBucket != "food" &&
                candidate.typeBucket != "nightlife" &&
                slot in preferredNonFoodSlots &&
                slot !in context.coveredNonFoodSlots
            ) 3.0 else 0.0

        val score = (candidate.voteScore * VOTE_WEIGHT_MULTIPLIER) -
                (tuning.travelPenaltyWeight * travelMinutes) -
                repeatPenalty -
                diversityPenalty +
                nonFoodBonus +
                slotCoverageBonus

        if (score > bestScore) {
            bestScore = score
            bestCandidate = candidate
            bestStartMinute = startMinute
            bestEndMinute = endMinute
        }
    }

    val candidate = bestCandidate ?: return null
    return Triple(candidate, bestStartMinute, bestEndMinute)
}

private fun appendPlannedActivity(
    context: DayPlanContext,
    candidate: ActivityCandidate,
    startMinute: Int,
    endMinute: Int,
    tuning: GenerationTuning
) {
    context.plan += PlannedActivity(
        activity = candidate.activity,
        startMinute = startMinute,
        endMinute = endMinute
    )

    context.currentMinute = endMinute + tuning.bufferMinutes
    context.lastActivity = candidate.activity
    context.lastTypeBucket = candidate.typeBucket

    if (candidate.typeBucket == "food") {
        slotForMinute(startMinute)?.let { context.usedFoodSlots += it }
    } else if (candidate.typeBucket != "nightlife") {
        context.nonFoodCount += 1
        slotForMinute(startMinute)?.let { context.coveredNonFoodSlots += it }
    }

    context.bucketCount[candidate.typeBucket] =
        (context.bucketCount[candidate.typeBucket] ?: 0) + 1
}

private fun fillWindowGreedy(
    candidates: MutableList<ActivityCandidate>,
    tuning: GenerationTuning,
    context: DayPlanContext,
    windowEndMinute: Int,
    deferredActivityIds: Set<String>,
    reservedSlotsToKeep: Int
) {
    while (
        candidates.isNotEmpty() &&
        context.currentMinute < windowEndMinute &&
        context.currentMinute < tuning.dayEndMinute &&
        context.plan.size < (tuning.maxActivitiesPerDay - reservedSlotsToKeep)
    ) {
        val choice = chooseBestCandidateForWindow(
            candidates = candidates,
            tuning = tuning,
            context = context,
            windowEndMinute = windowEndMinute,
            deferredActivityIds = deferredActivityIds
        )

        if (choice == null) {
            val nextBoundary = nextSlotBoundary(context.currentMinute) ?: break
            if (nextBoundary <= context.currentMinute || nextBoundary >= windowEndMinute) break
            context.currentMinute = nextBoundary
            continue
        }

        val (candidate, startMinute, endMinute) = choice
        appendPlannedActivity(context, candidate, startMinute, endMinute, tuning)
        candidates.remove(candidate)
    }
}

private fun generateDayPlan(
    candidates: MutableList<ActivityCandidate>,
    tuning: GenerationTuning,
    restaurantTarget: Int,
    dayStartMinute: Int = 9 * 60
): List<PlannedActivity> {
    val context = DayPlanContext(
        currentMinute = dayStartMinute,
        lastActivity = null,
        lastTypeBucket = null,
        nonFoodCount = 0,
        bucketCount = mutableMapOf(),
        usedFoodSlots = mutableSetOf(),
        coveredNonFoodSlots = mutableSetOf(),
        plan = mutableListOf()
    )

    val reservedMeal = reserveRestaurantForDay(
        candidates = candidates,
        restaurantTarget = maxOf(1, restaurantTarget)
    )
    val reservedAfternoon = reserveAfternoonNonFoodForDay(candidates)

    val deferredActivityIds = mutableSetOf<String>().apply {
        reservedMeal?.candidate?.activity?.id?.let(::add)
        reservedAfternoon?.candidate?.activity?.id?.let(::add)
    }

    logger.debug(
        "Reserved meal: title={} slot={} voteScore={}",
        reservedMeal?.candidate?.activity?.title,
        reservedMeal?.slot,
        reservedMeal?.candidate?.voteScore
    )
    logger.debug(
        "Reserved afternoon non-food: title={} voteScore={}",
        reservedAfternoon?.candidate?.activity?.title,
        reservedAfternoon?.candidate?.voteScore
    )
    if (reservedMeal == null && reservedAfternoon == null) {
        fillWindowGreedy(
            candidates = candidates,
            tuning = tuning,
            context = context,
            windowEndMinute = tuning.dayEndMinute,
            deferredActivityIds = emptySet(),
            reservedSlotsToKeep = 0
        )
        return context.plan
    }

    logger.debug(
        "Before meal insert: currentMinute={} planSize={} plan={}",
        context.currentMinute,
        context.plan.size,
        context.plan.map { it.activity.title }
    )

    val reservedAfternoonSlotStart = if (reservedAfternoon != null) {
        slotRanges()[DaySlot.AFTERNOON]?.first ?: tuning.dayEndMinute
    } else {
        tuning.dayEndMinute
    }

    // Fill morning window before the reserved afternoon anchor.
    fillWindowGreedy(
        candidates = candidates,
        tuning = tuning,
        context = context,
        windowEndMinute = reservedAfternoonSlotStart,
        deferredActivityIds = deferredActivityIds,
        reservedSlotsToKeep = deferredActivityIds.size
    )

    fun placeReservedMeal() {
        val meal = reservedMeal ?: return
        val reservedPlacement = computeReservedMealPlacement(
            reservedMeal = meal,
            context = context,
            tuning = tuning
        )
        logger.debug("Reserved meal placement: {}", reservedPlacement)

        if (reservedPlacement != null && context.plan.size < tuning.maxActivitiesPerDay) {
            val (mealStart, mealEnd) = reservedPlacement

            logger.debug(
                "Inserting reserved meal: {} at {}-{}",
                meal.candidate.activity.title,
                mealStart,
                mealEnd
            )

            appendPlannedActivity(
                context = context,
                candidate = meal.candidate,
                startMinute = mealStart,
                endMinute = mealEnd,
                tuning = tuning
            )

            candidates.remove(meal.candidate)
        } else {
            logger.debug(
                "Skipped reserved meal: reservedPlacement={} planSize={} maxActivitiesPerDay={}",
                reservedPlacement,
                context.plan.size,
                tuning.maxActivitiesPerDay
            )
        }
        deferredActivityIds.remove(meal.candidate.activity.id)
    }

    fun placeReservedAfternoon() {
        val afternoon = reservedAfternoon ?: return
        val reservedAfternoonPlacement = computeReservedAfternoonPlacement(
            reservedAfternoon = afternoon,
            context = context,
            tuning = tuning
        )
        logger.debug("Reserved afternoon placement: {}", reservedAfternoonPlacement)

        if (reservedAfternoonPlacement != null && context.plan.size < tuning.maxActivitiesPerDay) {
            val (afternoonStartMinute, afternoonEndMinute) = reservedAfternoonPlacement
            logger.debug(
                "Inserting reserved afternoon activity: {} at {}-{}",
                afternoon.candidate.activity.title,
                afternoonStartMinute,
                afternoonEndMinute
            )
            appendPlannedActivity(
                context = context,
                candidate = afternoon.candidate,
                startMinute = afternoonStartMinute,
                endMinute = afternoonEndMinute,
                tuning = tuning
            )
            candidates.remove(afternoon.candidate)
        } else {
            logger.debug(
                "Skipped reserved afternoon activity: reservedPlacement={} planSize={} maxActivitiesPerDay={}",
                reservedAfternoonPlacement,
                context.plan.size,
                tuning.maxActivitiesPerDay
            )
        }
        deferredActivityIds.remove(afternoon.candidate.activity.id)
    }

    val dinnerAnchorStart = slotRanges()[DaySlot.DINNER]?.first ?: tuning.dayEndMinute
    placeReservedAfternoon()

    // Fill from afternoon toward dinner while keeping the reserved meal available.
    fillWindowGreedy(
        candidates = candidates,
        tuning = tuning,
        context = context,
        windowEndMinute = dinnerAnchorStart,
        deferredActivityIds = deferredActivityIds,
        reservedSlotsToKeep = deferredActivityIds.size
    )
    placeReservedMeal()

    // Fill the remaining part of the day after the meal.
    fillWindowGreedy(
        candidates = candidates,
        tuning = tuning,
        context = context,
        windowEndMinute = tuning.dayEndMinute,
        deferredActivityIds = emptySet(),
        reservedSlotsToKeep = 0
    )

    return context.plan
}

private fun buildRestaurantTargets(dayCount: Int, totalFoodCandidates: Int): List<Int> {
    if (dayCount <= 0) return emptyList()

    var remainingFood = totalFoodCandidates.coerceAtLeast(0)
    val targets = MutableList(dayCount) { 0 }

    for (dayIndex in 0 until dayCount) {
        if (remainingFood <= 0) break
        targets[dayIndex] = 1
        remainingFood -= 1
    }

    for (dayIndex in 0 until dayCount) {
        if (remainingFood <= 0) break
        if (targets[dayIndex] >= 2) continue
        targets[dayIndex] += 1
        remainingFood -= 1
    }

    return targets
}

private suspend fun generateAndPersistItinerary(tripId: String) {
    val trip = fetchTripWithMembers(tripId) ?: return

    val activities = SupabaseConfig.client.from("activities")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<Activity>()

    if (activities.isEmpty()) return

    val votes = SupabaseConfig.client.from("votes")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<Vote>()

    val tuning = tuneForGroupVibe(trip.members.map { it.travelVibe })
    val candidates = buildCandidates(activities, votes)

    val startDate = parseIsoDateOrNull(trip.startDate)
    val endDate = parseIsoDateOrNull(trip.endDate)
    val tripDates = if (startDate != null && endDate != null) {
        datesBetweenInclusive(startDate, endDate)
    } else {
        listOf(LocalDate.now())
    }

    val dayRows = tripDates.mapIndexed { index, date ->
        val dayDate = date.toString()
        runCatching {
            SupabaseConfig.client.from("itinerary_days").insert(
                ItineraryDayInsert(
                    tripId = tripId,
                    dayDate = dayDate,
                    dayNumber = index + 1
                )
            ) { select() }
                .decodeList<ItineraryDayRow>()
                .first()
        }.getOrElse {
            SupabaseConfig.client.from("itinerary_days")
                .select {
                    filter {
                        eq("trip_id", tripId)
                        eq("day_date", dayDate)
                    }
                    limit(1)
                }
                .decodeList<ItineraryDayRow>()
                .firstOrNull()
                ?: throw it
        }
    }

    val restaurantTargets = buildRestaurantTargets(
        dayCount = dayRows.size,
        totalFoodCandidates = candidates.count { it.typeBucket == "food" }
    )

    logger.debug("Restaurant targets: {}", restaurantTargets)
    logger.debug("Initial food candidates: {}", candidates.count { it.typeBucket == "food" })

    dayRows.forEachIndexed { dayIndex, day ->
        val restaurantTarget = restaurantTargets.getOrElse(dayIndex) { 0 }
        logger.debug(
            "Day {} starting with {} remaining candidates, {} food remaining, target={}",
            dayIndex + 1,
            candidates.size,
            candidates.count { it.typeBucket == "food" },
            restaurantTarget
        )
        val planned = generateDayPlan(
            candidates = candidates,
            tuning = tuning,
            restaurantTarget = restaurantTarget
        )

        planned.forEachIndexed { idx, item ->
            SupabaseConfig.client.from("scheduled_activities").insert(
                ScheduledActivityInsert(
                    itineraryDayId = day.id,
                    activityId = item.activity.id,
                    startTime = formatDisplayTime(item.startMinute),
                    endTime = formatDisplayTime(item.endMinute),
                    notes = "",
                    sortOrder = idx
                )
            )
        }
    }
}

private suspend fun clearItineraryForTrip(tripId: String) {
    val dayRows = SupabaseConfig.client.from("itinerary_days")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<ItineraryDayRow>()

    val dayIds = dayRows.map { it.id }

    if (dayIds.isNotEmpty()) {
        SupabaseConfig.client.from("scheduled_activities")
            .delete { filter { isIn("itinerary_day_id", dayIds) } }
    }

    SupabaseConfig.client.from("itinerary_days")
        .delete { filter { eq("trip_id", tripId) } }
}

private suspend fun insertEventIntoGoogleCalendar(
    accessToken: String,
    calendarId: String,
    request: GoogleCalendarEventInsertRequest
): Boolean {
    val url = "https://www.googleapis.com/calendar/v3/calendars/${calendarId.encodeURLPathPart()}/events"
    return try {
        val response = googleCalendarHttpClient.post(url) {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(request)
        }
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            logger.warn(
                "Google Calendar insert failed status={} body={}",
                response.status.value,
                bodyText
            )
            return false
        }

        true
    } catch (t: Throwable) {
        logger.warn("Google Calendar insert exception: {}", t.message)
        false
    }
}

/**
 * Shared helper used by multiple routes: fetches an itinerary (days + scheduled activities)
 * for a trip, or null if there are no itinerary days yet.
 */
suspend fun fetchItineraryForTrip(tripId: String): Itinerary? {
    val dayRows = SupabaseConfig.client.from("itinerary_days")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<ItineraryDayRow>()
        .sortedBy { it.dayNumber }

    if (dayRows.isEmpty()) return null

    val activities = SupabaseConfig.client.from("activities")
        .select { filter { eq("trip_id", tripId) } }
        .decodeList<Activity>()
        .associateBy { it.id }

    val dayIds = dayRows.map { it.id }
    val scheduledRows = if (dayIds.isEmpty()) {
        emptyList()
    } else {
        SupabaseConfig.client.from("scheduled_activities")
            .select { filter { isIn("itinerary_day_id", dayIds) } }
            .decodeList<ScheduledActivityRow>()
    }

    val scheduledByDay = scheduledRows.groupBy { it.itineraryDayId }

    val days = dayRows.map { dayRow ->
        val scheduledForDay = (scheduledByDay[dayRow.id] ?: emptyList())
            .sortedWith(compareBy<ScheduledActivityRow> { it.sortOrder }.thenBy { it.startTime })
            .mapNotNull { sa ->
                val act = activities[sa.activityId] ?: return@mapNotNull null
                ScheduledActivity(
                    activity = act,
                    startTime = sa.startTime,
                    endTime = sa.endTime,
                    notes = sa.notes
                )
            }

        ItineraryDay(
            date = dayRow.dayDate,
            dayNumber = dayRow.dayNumber,
            activities = scheduledForDay
        )
    }

    return Itinerary(tripId = tripId, days = days)
}

private suspend fun fetchItinerary(tripId: String): Itinerary? = fetchItineraryForTrip(tripId)

fun Route.itineraryRoutes() {
    authenticate("supabase-jwt") {
        get("/trips/{id}/itinerary") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@get
            }

            call.userId

            try {
                val lock = tripGenerationLocks.getOrPut(tripId) { Mutex() }
                val response = lock.withLock {
                    val existing = fetchItinerary(tripId)
                    if (existing != null) {
                        existing
                    } else {
                        generateAndPersistItinerary(tripId)
                        fetchItinerary(tripId) ?: Itinerary(tripId = tripId, days = emptyList())
                    }
                }
                call.respond(response)
            } catch (t: Throwable) {
                logger.error("Failed to get/generate itinerary for tripId=$tripId", t)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Itinerary generation failed: ${t.message}"
                )
            }
        }

        post("/trips/{id}/itinerary/regenerate") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@post
            }

            call.userId

            try {
                val lock = tripGenerationLocks.getOrPut(tripId) { Mutex() }
                val regenerated = lock.withLock {
                    clearItineraryForTrip(tripId)
                    generateAndPersistItinerary(tripId)
                    fetchItinerary(tripId) ?: Itinerary(tripId = tripId, days = emptyList())
                }
                call.respond(regenerated)
            } catch (t: Throwable) {
                logger.error("Failed to regenerate itinerary for tripId=$tripId", t)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Itinerary regeneration failed: ${t.message}"
                )
            }
        }

        post("/trips/{id}/itinerary/export/google-calendar") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@post
            }

            call.userId

            val body = call.receive<ExportGoogleCalendarRequest>()
            if (body.googleAccessToken.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing Google access token")
                return@post
            }

            val itinerary = fetchItinerary(tripId) ?: run {
                call.respond(ExportGoogleCalendarResponse(created = 0, failed = 0))
                return@post
            }

            var created = 0
            var failed = 0

            itinerary.days.forEach { day ->
                day.activities.forEach { scheduled ->
                    val start = toGoogleRfc3339(
                        date = day.date,
                        time = scheduled.startTime,
                        timeZone = body.timeZone
                    )
                    val end = toGoogleRfc3339(
                        date = day.date,
                        time = scheduled.endTime,
                        timeZone = body.timeZone
                    )

                    if (start == null || end == null) {
                        logger.warn(
                            "Skipping itinerary activity during Google Calendar export due to invalid date/time tripId={} date={} title='{}' start='{}' end='{}'",
                            tripId,
                            day.date,
                            scheduled.activity.title,
                            scheduled.startTime,
                            scheduled.endTime
                        )
                        failed += 1
                        return@forEach
                    }

                    val details = buildList<String> {
                        val description = scheduled.activity.description?.trim().orEmpty()
                        if (description.isNotBlank()) add(description)
                        val notes = scheduled.notes.trim()
                        if (notes.isNotBlank()) add("Notes: $notes")
                    }.joinToString("\n\n").ifBlank { null }

                    val createdEvent = insertEventIntoGoogleCalendar(
                        accessToken = body.googleAccessToken,
                        calendarId = body.calendarId.ifBlank { "primary" },
                        request = GoogleCalendarEventInsertRequest(
                            summary = scheduled.activity.title,
                            location = scheduled.activity.location.ifBlank { null },
                            description = details,
                            start = GoogleCalendarEventDateTime(
                                dateTime = start,
                                timeZone = body.timeZone
                            ),
                            end = GoogleCalendarEventDateTime(
                                dateTime = end,
                                timeZone = body.timeZone
                            )
                        )
                    )

                    if (createdEvent) created += 1 else failed += 1
                }
            }

            call.respond(
                ExportGoogleCalendarResponse(
                    created = created,
                    failed = failed
                )
            )
        }

        patch("/trips/{id}/itinerary/days/{date}/activities") {
            val tripId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing trip id")
                return@patch
            }

            val date = call.parameters["date"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing date")
                return@patch
            }

            call.userId

            val body = call.receive<UpdateScheduledActivityRequest>()

            val day = SupabaseConfig.client.from("itinerary_days")
                .select {
                    filter {
                        eq("trip_id", tripId)
                        eq("day_date", date)
                    }
                    limit(1)
                }
                .decodeList<ItineraryDayRow>()
                .firstOrNull()

            if (day == null) {
                call.respond(HttpStatusCode.NotFound, "Itinerary day not found")
                return@patch
            }

            val updated = SupabaseConfig.client.from("scheduled_activities").update({
                set("start_time", body.startTime)
                set("end_time", body.endTime)
                set("notes", body.notes)
            }) {
                filter {
                    eq("itinerary_day_id", day.id)
                    eq("activity_id", body.activityId)
                }
                select()
            }.decodeList<ScheduledActivityRow>()

            if (updated.isEmpty()) {
                SupabaseConfig.client.from("scheduled_activities").insert(
                    ScheduledActivityInsert(
                        itineraryDayId = day.id,
                        activityId = body.activityId,
                        startTime = body.startTime,
                        endTime = body.endTime,
                        notes = body.notes
                    )
                )
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
