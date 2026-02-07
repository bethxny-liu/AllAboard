package org.allaboard.project.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.*

class TravelVibeScreen : Screen {
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        TravelVibeContent(onNext = { navigator?.push(BudgetScreen()) }, onBack = { navigator?.pop() })
    }
}

@Composable
fun TravelVibeContent(onNext: () -> Unit = {}, onBack: () -> Unit = {}) {
    var selected by remember { mutableStateOf<String?>(null) }
    val inspection = LocalInspectionMode.current
    val visible = remember { mutableStateOf(inspection) }
    LaunchedEffect(Unit) { if (!inspection) visible.value = true }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
        // App bar: logo + progress (no back on top)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(Res.drawable.logo), contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(8.dp))
            Text("All Aboard", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Text("1 of 3", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }

        AnimatedVisibility(visible = visible.value, enter = fadeIn()) {
            Column {
                Spacer(Modifier.height(12.dp))

                Text("What's your travel vibe?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    VibeCard(
                        iconPainter = painterResource(Res.drawable.vibe_relaxed),
                        title = "Relaxed",
                        subtitle = "I like to take it easy",
                        selected = selected == "relaxed",
                        onClick = { selected = "relaxed" }
                    )
                    VibeCard(
                        iconPainter = painterResource(Res.drawable.vibe_adventurous),
                        title = "Adventurous",
                        subtitle = "I'm up for anything",
                        selected = selected == "adventurous",
                        onClick = { selected = "adventurous" }
                    )
                    VibeCard(
                        iconPainter = painterResource(Res.drawable.vibe_balanced),
                        title = "Balanced",
                        subtitle = "A mix of chill and thrill",
                        selected = selected == "balanced",
                        onClick = { selected = "balanced" }
                    )
                }

                Spacer(Modifier.weight(1f))

                // Bottom nav: Back (left) and Next (right) side-by-side
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 0.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BackPill(onClick = { onBack() }, modifier = Modifier.weight(1f).height(56.dp))

                    Button(
                        onClick = { if (selected != null) onNext() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = selected != null,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = TextPrimary)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}

@Preview(name = "TravelVibeMobile", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun TravelVibePreview() {
    AppTheme { TravelVibeContent() }
}
