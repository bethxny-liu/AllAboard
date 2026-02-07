package org.allaboard.project.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.screens.HomeScreen
import org.allaboard.project.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.*

class InterestsScreen : Screen {
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        InterestsContent(onFinish = { navigator?.push(HomeScreen()) }, onBack = { navigator?.pop() })
    }
}

@Composable
fun InterestsContent(onFinish: () -> Unit = {}, onBack: () -> Unit = {}) {
    var selected by remember { mutableStateOf(setOf<String>()) }
    // Preview environments sometimes don't execute LaunchedEffect which leaves AnimatedVisibility hidden.
    // Make the content visible by default to ensure previews render. This still allows us to wire animations
    // from navigation events if needed later.
    val visible = remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
        // App bar (logo + title, no back on top)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(Res.drawable.logo), contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(8.dp))
            Text("All Aboard", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Text("3 of 3", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }

        AnimatedVisibility(visible = visible.value, enter = fadeIn()) {
            Column {
                Spacer(Modifier.height(12.dp))

                Text("What are you into?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)

                Spacer(Modifier.height(16.dp))

                // Two-column grid
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val options = listOf("Food & Drink", "Arts & Culture", "Nightlife", "Outdoors", "Shopping", "Sightseeing")
                    for (row in options.chunked(2)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (opt in row) {
                                Box(modifier = Modifier.weight(1f)) {
                                    InterestChip(label = opt, selected = selected.contains(opt), onClick = {
                                        selected = if (selected.contains(opt)) selected - opt else selected + opt
                                    })
                                }
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Bottom nav: Back (left) and Finish (right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BackPill(onClick = { onBack() }, modifier = Modifier.weight(1f).height(56.dp))

                    OutlinedButton(
                        onClick = onFinish,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = selected.isNotEmpty(),
                        shape = RoundedCornerShape(28.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}

@Preview(name = "InterestsMobile", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun InterestsPreview() {
    AppTheme { InterestsContent() }
}
