package org.allaboard.project.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.screens.createTrip.CreateTripScreen
import org.allaboard.project.ui.screens.tripHome.TripHomeScreen
import org.allaboard.project.ui.theme.Background
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextHint
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.logo
import org.allaboard.project.di.AppModule

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: HomeViewModel = viewModel { HomeViewModel(model = AppModule.allAboardModel) }
        val uiState by viewModel.uiState.collectAsState(initial = viewModel.uiState.value)

        HomeScreenContent(
            uiState = uiState,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onTripClick = { tripId -> navigator?.push(TripHomeScreen(tripId)) },
            onCreateTripClick = { navigator?.push(CreateTripScreen()) }
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onTripClick: (String) -> Unit,
    onCreateTripClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 100.dp)
        ) {
            // Header with logo and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "All Aboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text("Search", color = TextHint)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = FieldBackground,
                    focusedContainerColor = FieldBackground,
                    unfocusedBorderColor = FieldBackground,
                    focusedBorderColor = TextSecondary
                ),
                singleLine = true
            )

            Spacer(Modifier.height(32.dp))

            // Upcoming Trips section
            Text(
                text = "Upcoming Trips",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            uiState.upcomingTrips.forEach { trip ->
                HomeCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) }
                )
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Past Trips section
            Text(
                text = "Past Trips",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            uiState.pastTrips.forEach { trip ->
                HomeCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Floating action button
        FloatingActionButton(
            onClick = onCreateTripClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
            shape = CircleShape,
            containerColor = Surface
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create Trip",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
