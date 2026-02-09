package org.allaboard.project.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.allaboard.project.ui.theme.FieldBackground
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.logo

@Composable
fun InterestsStep(
    vm: OnboardingViewModel,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val state = vm.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 45.dp,
                bottom = 40.dp
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with logo and step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "All Aboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    "3 of 3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(30.dp))

            // Main title (centered, matching CreateTrip style)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "What are you into?",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(30.dp))

            // Interest chips grid (equal columns via BoxWithConstraints, no weight)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val options = listOf(
                    "Food & Drink",
                    "Arts & Culture",
                    "Nightlife",
                    "Outdoors",
                    "Shopping",
                    "Sightseeing"
                )
                for (row in options.chunked(2)) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val cellWidth = (maxWidth - 12.dp) / 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (opt in row) {
                                Box(modifier = Modifier.width(cellWidth)) {
                                    InterestChip(
                                        label = opt,
                                        selected = state.interests.contains(opt),
                                        onClick = { vm.toggleInterest(opt) }
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.width(cellWidth))
                        }
                    }
                }
            }
        }

        // Bottom buttons positioned at bottom (no weight needed)
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            val buttonWidth = (maxWidth - 12.dp) / 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .width(buttonWidth)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FieldBackground,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .width(buttonWidth)
                        .height(48.dp),
                    enabled = state.interests.isNotEmpty(),
                ) {
                    Text("Finish", color = Color.Black)
                }
            }
        }
    }
}
