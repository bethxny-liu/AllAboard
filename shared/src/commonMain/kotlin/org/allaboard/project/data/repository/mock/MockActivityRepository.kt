package org.allaboard.project.data.repository.mock

import kotlinx.coroutines.delay
import org.allaboard.project.data.repository.ActivityRepository
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.BudgetLevel

class MockActivityRepository(
    private val userRepository: MockUserRepository? = null
) : ActivityRepository {
    private val activities = mutableListOf(
        Activity(
            id = "act-1",
            title = "Senso-ji Temple",
            location = "Asakusa, Tokyo",
            description = "Historic temple with vibrant market streets and iconic Kaminarimon gate.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Senso-ji",
            voteCount = 0,
            type = ActivityType.LANDMARK
        ),
        Activity(
            id = "act-2",
            title = "Ichiran Ramen",
            location = "Shibuya, Tokyo",
            description = "Solo-booth ramen experience known for rich tonkotsu broth.",
            rating = 4.5f,
            priceLevel = "$",
            mapPinLabel = "Ichiran",
            voteCount = 0,
            type = ActivityType.RESTAURANT
        ),
        Activity(
            id = "act-3",
            title = "Mount Fuji Day Trip",
            location = "Yamanashi Prefecture",
            description = "Scenic day trip with lake views and photo spots near Fuji.",
            rating = 4.8f,
            priceLevel = "$$$",
            mapPinLabel = "Fuji",
            voteCount = 0,
            type = ActivityType.EXPERIENCES
        ),
        Activity(
            id = "act-4",
            title = "Shibuya Crossing",
            location = "Shibuya, Tokyo",
            description = "World's busiest pedestrian crossing and iconic Tokyo landmark.",
            rating = 4.6f,
            priceLevel = "$",
            mapPinLabel = "Shibuya",
            voteCount = 0,
            type = ActivityType.LANDMARK
        ),
        Activity(
            id = "act-5",
            title = "Tsukiji Outer Market",
            location = "Chuo City, Tokyo",
            description = "Fresh sushi and seafood market experience.",
            rating = 4.7f,
            priceLevel = "$$",
            mapPinLabel = "Tsukiji",
            voteCount = 0,
            type = ActivityType.RESTAURANT
        )
    )

    private val tripActivities = mutableMapOf(
        "trip-1" to mutableListOf("act-1", "act-2", "act-3", "act-4", "act-5")
    )

    override suspend fun getActivity(activityId: String): Activity? {
        delay(50)
        return activities.find { it.id == activityId }
    }

    override suspend fun getActivitiesForTrip(tripId: String): List<Activity> {
        delay(100)
        val activityIds = tripActivities[tripId] ?: return emptyList()
        return activities.filter { it.id in activityIds }
    }

    override suspend fun addActivity(tripId: String, activity: Activity) {
        delay(100)
        activities.add(activity)
        tripActivities.getOrPut(tripId) { mutableListOf() }.add(activity.id)
    }

    override suspend fun updateActivity(activity: Activity) {
        delay(50)
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index != -1) {
            activities[index] = activity
        }
    }

    override suspend fun deleteActivity(activityId: String) {
        delay(50)
        activities.removeAll { it.id == activityId }
    }

    /**
     * Simulates backend recommendation logic.
     * In Sprint 3, this will be a backend API call.
     */
    override suspend fun getRecommendedActivities(tripId: String, userId: String): List<Activity> {
        delay(100)
        val user = userRepository?.getUser(userId) ?: return getActivitiesForTrip(tripId)
        val tripActivitiesList = getActivitiesForTrip(tripId)

        return tripActivitiesList.filter { activity ->
            matchesBudget(activity, user.budget)
        }
    }

    private fun matchesBudget(activity: Activity, budget: BudgetLevel): Boolean {
        return when (budget) {
            BudgetLevel.LOW -> activity.priceLevel in listOf("$", "$$")
            BudgetLevel.MEDIUM -> activity.priceLevel in listOf("$$", "$$$")
            BudgetLevel.HIGH -> true
        }
    }
}
