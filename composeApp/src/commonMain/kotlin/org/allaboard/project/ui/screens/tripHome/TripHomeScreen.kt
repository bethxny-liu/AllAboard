package org.allaboard.project.ui.screens.tripHome

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.domain.Activity
import org.allaboard.project.domain.ActivityType
import org.allaboard.project.ui.screens.activityDetails.ActivityDetailsScreen
import org.allaboard.project.ui.screens.createTrip.CreateTripScreen
import org.allaboard.project.ui.screens.createTrip.CreateTripViewModel
import org.allaboard.project.ui.screens.tripHome.swipe.SwipingScreen
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.MintAccent
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.prettyplace

class TripHomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = viewModel { TripHomeViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        TripHomeScreenContent(
            uiState = uiState,
            viewModel = viewModel,
            onStartSwipingClick = { navigator?.push(SwipingScreen(uiState.activities)) },
            onEditTrip = {
                navigator?.push(
                    CreateTripScreen(
                        mode = CreateTripViewModel.Mode.Edit,
                        tripId = uiState.trip.id,
                        startStep = 0
                    )
                )
            },
            onActivitySelected = { activity ->
                navigator?.push(ActivityDetailsScreen(activity, activity.id))
            }
        )
    }
}

@Composable
fun TripHomeScreenContent(
    uiState: TripHomeUiState,
    viewModel: TripHomeViewModel,
    onEditTrip: () -> Unit,
    onActivitySelected: (Activity) -> Unit,
    onStartSwipingClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Section with Trip Background and Info
        TripHeroSection(
            trip = uiState.trip,
            onEditClick = onEditTrip,
            onStartSwipingClick = onStartSwipingClick,
            onViewItineraryClick = viewModel::onViewItineraryClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Landmarks Section
        SectionWithCards(
            title = "Landmarks",
            items = uiState.activities.filter { it.type == ActivityType.LANDMARK },
            itemId = { it.id },
            onItemClick = onActivitySelected
        ) { landmark ->
            ActivityCard(
                title = landmark.title,
                voteCount = landmark.voteCount,
                imageContent = {
                    Image(
                        painter = painterResource(Res.drawable.prettyplace),
                        contentDescription = null,
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
            title = "Restaurants & Food",
            items = uiState.activities.filter { it.type == ActivityType.RESTAURANT },
            itemId = { it.id },
            onItemClick = onActivitySelected
        ) { restaurant ->
            ActivityCard(
                title = restaurant.title,
                voteCount = restaurant.voteCount,
                imageContent = {
                    Image(
                        painter = painterResource(Res.drawable.prettyplace),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Activities & Experiences Section
        SectionWithCards(
            title = "Activities & Experiences",
            items = uiState.activities.filter { it.type == ActivityType.ACTIVITY },
            itemId = { it.id },
            onItemClick = onActivitySelected
        ) { activity ->
            ActivityCard(
                title = activity.title,
                voteCount = activity.voteCount,
                imageContent = {
                    Image(
                        painter = painterResource(Res.drawable.prettyplace),
                        contentDescription = null,
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
    onStartSwipingClick: () -> Unit,
    onViewItineraryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
    ) {
        // Background hero image spans edge-to-edge
        Image(
            painter = painterResource(Res.drawable.prettyplace),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay content with insets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Button(
                onClick = onEditClick,
                modifier = Modifier.align(Alignment.TopEnd),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Surface)
            ) {
                Text("Edit", color = TextPrimary)
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
                    text = trip.dateRange,
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
                    Text(
                        text = "👥 ${trip.memberCount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Surface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
