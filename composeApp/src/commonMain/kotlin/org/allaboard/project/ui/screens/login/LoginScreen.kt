package org.allaboard.project.ui.screens.login

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.screens.HomeScreen
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            // TODO: Add login_background image
            Box(
                modifier = Modifier
                    .fillMaxSize()
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo and title section
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Logo image
                        // TODO: Add logo image
                        Box(
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // App name
                        Text(
                            "All Aboard",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        // Subtitle
                        Text(
                            "Your next group adventure\nstarts here",
                            fontSize = 16.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                // Sign in button
                Button(
                    onClick = {
                        navigator?.push(HomeScreen())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Google Logo
                        // TODO: Add Google logo image
                        Box(
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Sign in with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}