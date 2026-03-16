package org.allaboard.project.ui.screens.tripHome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.Category
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.displayDateRange
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.allaboard.project.ui.components.NetworkImage
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen
import org.allaboard.project.ui.screens.createTrip.CreateTripScreen
import org.allaboard.project.ui.screens.createTrip.CreateTripViewModel
import org.allaboard.project.ui.screens.createActivity.CreateCustomActivityScreen
import org.allaboard.project.ui.screens.tripHome.swipe.SwipingScreen
import org.allaboard.project.ui.screens.itinerary.ItineraryScreen
import org.allaboard.project.ui.theme.MintAccent
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.di.AppModule

class TripHomeScreen(private val tripId: String) : Screen {

    // Unique key per tripId so Voyager treats each trip as a different screen
    override val key: cafe.adriel.voyager.core.screen.ScreenKey
        get() = "TripHomeScreen-$tripId"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: TripHomeViewModel = viewModel {
            TripHomeViewModel(
                model = AppModule.allAboardModel,
                tripId = tripId
            )
        }
        val uiState by viewModel.uiState.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.tripDeleted) {
            if (uiState.tripDeleted) navigator?.pop()
        }

        // If trip is null (should not happen), render the UI with a lightweight placeholder and the trip details will be filled in when the model returns.
        val tripNonNull: Trip = uiState.trip ?: Trip(
            id = tripId,
            title = "",
            destination = "",
            region = "",
            startDate = "",
            endDate = ""
        )

        TripHomeScreenContent(
            trip = tripNonNull,
            activities = uiState.activities,
            onStartSwipingClick = { navigator?.push(SwipingScreen(tripNonNull.id)) },
            onEditTrip = {
                navigator?.push(
                    CreateTripScreen(
                        mode = CreateTripViewModel.Mode.Edit,
                        tripId = tripNonNull.id,
                        startStep = 0
                    )
                )
            },
            onDeleteTrip = { showDeleteDialog = true },
            onCreateCustomActivity = { navigator?.push(CreateCustomActivityScreen(tripNonNull.id)) },
            onActivitySelected = { activity -> navigator?.push(ActivityDetailsScreen(activity, activity.id)) },
            onViewItinerary = { navigator?.push(ItineraryScreen(tripNonNull.id)) }
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete trip?") },
                text = { Text("This cannot be undone. The trip and all its activities will be permanently deleted.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTrip()
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }
    }
}

@Composable
fun TripHomeScreenContent(
    trip: Trip,
    activities: List<Activity>,
    onCreateCustomActivity: () -> Unit,
    onEditTrip: () -> Unit,
    onDeleteTrip: () -> Unit,
    onActivitySelected: (Activity) -> Unit,
    onStartSwipingClick: () -> Unit,
    onViewItinerary: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Section with Trip Background and Info
        TripHeroSection(
            trip = trip,
            onEditClick = onEditTrip,
            onDeleteClick = onDeleteTrip,
            onStartSwipingClick = onStartSwipingClick,
            onCreateCustomActivity = onCreateCustomActivity,
            onViewItineraryClick = onViewItinerary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Landmarks Section
        SectionWithCards(
            title = Category.LANDMARKS.displayName,
            items = activities.filter { it.type == ActivityType.LANDMARK },
            itemId = { it.id },
            onItemClick = onActivitySelected
        ) { landmark ->
            ActivityCard(
                title = landmark.title,
                voteCount = landmark.voteCount,
                imageContent = {
                    NetworkImage(
                        imageUrl = landmark.imageUrl,
                        contentDescription = landmark.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Restaurants & Food Section
        SectionWithCards(
            title = Category.RESTAURANTS.displayName,
            items = activities.filter { it.type == ActivityType.RESTAURANT },
            itemId = { it.id },
            onItemClick = onActivitySelected
        ) { restaurant ->
            ActivityCard(
                title = restaurant.title,
                voteCount = restaurant.voteCount,
                imageContent = {
                    NetworkImage(
                        imageUrl = restaurant.imageUrl,
                        contentDescription = restaurant.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Experiences Section
        SectionWithCards(
            title = Category.EXPERIENCES.displayName,
            items = activities.filter { it.type == ActivityType.EXPERIENCES },
            itemId = { it.id },
            onItemClick = onActivitySelected
        ) { activity ->
            ActivityCard(
                title = activity.title,
                voteCount = activity.voteCount,
                imageContent = {
                    NetworkImage(
                        imageUrl = activity.imageUrl,
                        contentDescription = activity.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TripHeroSection(
    trip: Trip,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStartSwipingClick: () -> Unit,
    onViewItineraryClick: () -> Unit,
    onCreateCustomActivity: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp),
    ) {
        // Background hero image spans edge-to-edge
        NetworkImage(
            imageUrl = trip.imageUrl,
            contentDescription = trip.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay content with insets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Surface)
                ) {
                    Text("Edit", color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More options",
                        tint = Surface
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = TextPrimary) },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Delete trip", color = Color.Red)
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {

                // Trip Title
                Text(
                    text = trip.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Surface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Range
                Text(
                    text = trip.displayDateRange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Surface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Member Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "Trip members",
                        tint = Surface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trip.memberCount.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Surface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartSwipingClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintAccent
                        )
                    ) {
                        Text(
                            text = "Start Swiping",
                            fontWeight = FontWeight.SemiBold,
                            color = Surface
                        )
                    }

                    Button(
                        onClick = onViewItineraryClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Surface
                        )
                    ) {
                        Text(
                            text = "View Itinerary",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Create Custom Activity under the action buttons, matching View Itinerary styling
                Button(
                    onClick = onCreateCustomActivity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Surface)
                ) {
                    Text(text = "Create Custom Activity", color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(24.dp))


            }
        }
    }
}

@Composable
private fun <T> SectionWithCards(
    title: String,
    items: List<T>,
    itemId: (T) -> String,
    onItemClick: (T) -> Unit,
    cardContent: @Composable (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section Header with Title and See All Link
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Scrollable Cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = itemId) { item ->
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .clickable { onItemClick(item) }
                ) {
                    cardContent(item)
                }
            }
        }
    }
}
