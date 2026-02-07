package org.allaboard.project.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.*
import androidx.compose.ui.draw.clip

@Suppress("unused")
class WelcomeScreen : Screen {
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        WelcomeContent(onGetStarted = { navigator?.push(TravelVibeScreen()) }, onBack = { navigator?.pop() })
    }
}

// New: stateless content composable so Preview is stable and we can pass a simple lambda for navigation
@Composable
fun WelcomeContent(
    modifier: Modifier = Modifier,
    onGetStarted: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val inspection = LocalInspectionMode.current
    val visible = remember { mutableStateOf(inspection) }
    LaunchedEffect(Unit) { if (!inspection) visible.value = true }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 })
    ) {
        // add system-safe padding so status bar and nav bar don't overlap content
        Column(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // Top app bar area (back + logo + title)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // removed BackPill to prevent back navigation from the welcome screen

                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("All Aboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // Hero image
            Image(
                painter = painterResource(Res.drawable.welcome_crew),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xFFF9FAFB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Welcome aboard,\nSpongebob!",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Let’s get to know your travel style so we can plan the perfect trips for you.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280)
                            )

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = onGetStarted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = TextPrimary)
                            ) {
                                Text("Get Started")
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomePreview() {
    AppTheme {
        WelcomeContent()
    }
}

@Preview(name = "WelcomeMobile", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun WelcomeMobilePreview() {
    AppTheme {
        WelcomeContent()
    }
}
