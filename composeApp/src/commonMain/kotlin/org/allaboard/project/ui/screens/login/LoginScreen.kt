package org.allaboard.project.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.di.AppModule
import org.allaboard.project.ui.screens.home.HomeScreen
import org.allaboard.project.ui.screens.onboarding.OnboardingScreen
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.logo
import team_102_8.composeapp.generated.resources.google_logo
import team_102_8.composeapp.generated.resources.login_background

class LoginScreen : Screen {
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: LoginViewModel = viewModel {
            LoginViewModel(model = AppModule.allAboardModel)
        }
        val uiState by viewModel.uiState.collectAsState()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            Image(
                painter = painterResource(Res.drawable.login_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(300.dp))

                // Logo and title section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Logo and App name on same line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Logo image
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        // App name
                        Text(
                            "All Aboard",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("app_title")
                        )
                    }


                    // Subtitle
                    Text(
                        "Your next group adventure\nstarts here",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("subtitle")
                    )
                }
                Spacer(Modifier.weight(1f))

                // Sign in button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Temporary sign up button to jump into onboarding
                    Button(
                        onClick = { navigator?.push(OnboardingScreen()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(28.dp))
                            .testTag("sign_up_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFECEFF1),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Sign up (Onboarding)")
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.signIn { navigator?.replace(HomeScreen()) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(28.dp))
                            .testTag("sign_in_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Surface,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Google Logo
                            Image(
                                painter = painterResource(Res.drawable.google_logo),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                modifier = Modifier.testTag("sign_in_button_text")
                            )
                        }
                    }
                }
            }
        }
    }
}