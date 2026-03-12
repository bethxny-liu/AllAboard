package org.allaboard.project.ui.screens.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.lifecycle.viewmodel.compose.viewModel
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.Activity
import org.allaboard.project.ui.components.ScreenTopBar
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen

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
                navigator?.push(ActivityDetailsScreen(activity = activity, fallbackActivityId = activity.id))
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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreenTopBar(title = "Itinerary", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 0.dp)
        ) {
            // Export button
            Button(
                onClick = {
                    // TODO: implement export to Google Calendar
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Export",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Export to Google Calendar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (uiState.days.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No itinerary items yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // List of days
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 0.dp)
                ) {
                    items(uiState.days) { day ->
                        Text(
                            text = "Day ${day.dayNumber}: ${day.date}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 8.dp)
                        )

                        day.activities.forEach { scheduledActivity ->
                            val bg = MaterialTheme.colorScheme.surfaceVariant
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(56.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .clickable { onActivityClick(scheduledActivity.activity) },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${scheduledActivity.startTime} ${scheduledActivity.activity.title}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
