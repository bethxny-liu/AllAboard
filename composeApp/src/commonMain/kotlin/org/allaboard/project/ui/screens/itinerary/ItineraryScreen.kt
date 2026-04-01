package org.allaboard.project.ui.screens.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.lifecycle.viewmodel.compose.viewModel
import org.allaboard.project.Category
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.ItineraryDay
import org.allaboard.project.domain.ScheduledActivity
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen
import org.allaboard.project.ui.theme.BluePrimaryDark
import org.allaboard.project.ui.theme.CoralAccent
import org.allaboard.project.ui.theme.GreenAccent

class ItineraryScreen(private val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: ItineraryViewModel = viewModel {
            ItineraryViewModel(tripId = tripId, model = AppModule.allAboardModel)
        }
        val uiState by viewModel.uiState.collectAsState()

        ItineraryContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onActivityClick = { activity ->
                navigator?.push(ActivityDetailsScreen(tripId = tripId, activity = activity, fallbackActivityId = activity.id))
            }
        )
    }
}

@Composable
fun ItineraryContent(
    onBack: () -> Unit,
    uiState: ItineraryUiState,
    onActivityClick: (Activity) -> Unit = {}
) {
    var selectedDayIndex by remember(uiState.days) { mutableIntStateOf(0) }
    val selectedDay = uiState.days.getOrNull(selectedDayIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Itinerary",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
            }
            return@Column
        }

        if (uiState.days.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No itinerary items yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        DayTabs(
            days = uiState.days,
            selectedIndex = selectedDayIndex,
            onSelected = { selectedDayIndex = it }
        )

        Spacer(Modifier.height(40.dp))

        if (selectedDay == null || selectedDay.activities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No activities for this day",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            itemsIndexed(selectedDay.activities) { index, scheduled ->
                TimelineItem(
                    scheduledActivity = scheduled,
                    isLast = index == selectedDay.activities.lastIndex,
                    onClick = { onActivityClick(scheduled.activity) }
                )
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun DayTabs(
    days: List<ItineraryDay>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        itemsIndexed(days) { index, day ->
            val isSelected = index == selectedIndex
            val bg = if (isSelected) Color(0xFFE9EEF5) else Color.Transparent
            val textColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(bg)
                    .clickable { onSelected(index) }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Day ${day.dayNumber}",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            if (index != days.lastIndex) {
                Text(
                    text = "|",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    scheduledActivity: ScheduledActivity,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.width(100.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFCDE7FF))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = formatTimeLabel(scheduledActivity.startTime),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 1.dp,
                shadowElevation = 5.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scheduledActivity.activity.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(6.dp))
                        val (label, tint) = activityTag(scheduledActivity.activity.type)
                        Text(
                            text = label,
                            color = tint,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Light,
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isLast) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(start = 5.dp, top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(Color(0xFFB8B8B8), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

private fun activityTag(type: ActivityType): Pair<String, Color> {
    return when (type) {
        ActivityType.LANDMARK -> Category.fromType(type).displayName to BluePrimaryDark
        ActivityType.RESTAURANT -> Category.fromType(type).displayName to CoralAccent
        ActivityType.EXPERIENCES -> Category.fromType(type).displayName to GreenAccent
    }
}

private fun formatTimeLabel(raw: String): String {
    val trimmed = raw.trim()

    // Handle backend times like "08:00:00" or "14:30:00"
    Regex("""^(\d{1,2}):(\d{2})(?::\d{2})?$""").matchEntire(trimmed)?.let { match ->
        val hour24 = match.groupValues[1].toIntOrNull() ?: return@let
        val minute = match.groupValues[2]
        val meridiem = if (hour24 >= 12) "pm" else "am"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return "$hour12:$minute $meridiem"
    }

    // Handle already-12h values like "9:00AM", "9:00 AM", etc.
    val compact = trimmed.replace(Regex("\\s+"), " ")
    val spaced = compact.replace(Regex("(?i)(\\d)(am|pm)$"), "$1 $2")
    return spaced.replace(Regex("(?i)\\b(am|pm)\\b")) { it.value.lowercase() }
}
