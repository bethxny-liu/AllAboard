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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import org.allaboard.project.di.AppModule
import org.allaboard.project.ui.screens.tripHome.TripHomeScreen
import org.allaboard.project.ui.theme.Background
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.TextHint
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import org.allaboard.project.ui.theme.fuzzyBubblesFontFamily
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.logo

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: HomeViewModel = viewModel { HomeViewModel(model = AppModule.allAboardModel) }
        val uiState by viewModel.uiState.collectAsState(initial = viewModel.uiState.value)

        HomeScreenContent(
            uiState = uiState,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onTripClick = { tripId -> navigator?.push(TripHomeScreen(tripId)) }
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onTripClick: (String) -> Unit
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
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = fuzzyBubblesFontFamily()
                    ),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            // Greeting above search bar (same style as "Welcome aboard!" on onboarding)
            Text(
                text = if (uiState.displayName.isNotBlank()) "Where to, ${uiState.displayName}?" else "Where to?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

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

    }
}
