package org.allaboard.project.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.fuzzyBubblesFontFamily
import org.allaboard.project.domain.TravelVibe
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.logo
import team_102_8.composeapp.generated.resources.vibe_adventurous
import team_102_8.composeapp.generated.resources.vibe_balanced
import team_102_8.composeapp.generated.resources.vibe_relaxed

@Composable
fun TravelVibeStep(
    uiState: OnboardingUiState,
    vm: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val inspection = LocalInspectionMode.current
    val visible = remember { mutableStateOf(inspection) }
    LaunchedEffect(Unit) { if (!inspection) visible.value = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 45.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "All Aboard",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = fuzzyBubblesFontFamily()
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(
                "1 of 3",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        AnimatedVisibility(visible = visible.value, enter = fadeIn()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "What's your travel vibe?",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pick the one that fits you best",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    VibeCard(
                        iconPainter = painterResource(Res.drawable.vibe_relaxed),
                        title = "Relaxed",
                        subtitle = "I like to take it easy",
                        selected = uiState.vibe == TravelVibe.RELAXED,
                        onClick = { vm.updateVibe(TravelVibe.RELAXED) }
                    )
                    VibeCard(
                        iconPainter = painterResource(Res.drawable.vibe_adventurous),
                        title = "Adventurous",
                        subtitle = "I'm up for anything",
                        selected = uiState.vibe == TravelVibe.ADVENTUROUS,
                        onClick = { vm.updateVibe(TravelVibe.ADVENTUROUS) }
                    )
                    VibeCard(
                        iconPainter = painterResource(Res.drawable.vibe_balanced),
                        title = "Balanced",
                        subtitle = "A mix of chill and thrill",
                        selected = uiState.vibe == TravelVibe.BALANCED,
                        onClick = { vm.updateVibe(TravelVibe.BALANCED) }
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FieldBackground,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                    ) {
                        Text("Next", color = Color.Black)
                    }
                }
            }
        }
    }
}
