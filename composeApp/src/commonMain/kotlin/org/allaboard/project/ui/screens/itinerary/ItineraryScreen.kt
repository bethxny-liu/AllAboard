package org.allaboard.project.ui.screens.itinerary

import androidx.compose.foundation.background
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

class ItineraryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: ItineraryViewModel = viewModel<ItineraryViewModel>()
        val uiState by viewModel.uiState.collectAsState(initial = viewModel.uiState.value)

        ItineraryContent(
            uiState = uiState,
            onBack = { navigator?.pop() }
        )
    }
}

@Composable
fun ItineraryContent(onBack: () -> Unit, uiState: ItineraryUiState) {
    // Top row matches CreateCustomActivityScreen: back button + title with spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "Itinerary",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
    }

    // Show selected date range using uiState to avoid unused parameter warning
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "${uiState.rangeStart} - ${uiState.rangeEnd}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 16.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 104.dp, bottom = 0.dp)
    ) {
        // Export button (replaces previous calendar image). Uses project theme colors and rounded shape.
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

        // List of days
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp)
        ) {
            items(uiState.days) { day ->
                Text(
                    text = day.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 8.dp)
                )

                day.items.forEach { item ->
                    val bg = MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "${item.time} ${item.title}",
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
