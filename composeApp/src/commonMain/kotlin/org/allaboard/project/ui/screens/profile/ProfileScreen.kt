package org.allaboard.project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.di.AppModule
import org.allaboard.project.ui.components.ScreenTopBar
import org.allaboard.project.ui.components.ScreenTopBarDefaults
import org.allaboard.project.ui.screens.login.LoginScreen
import org.allaboard.project.ui.screens.onboarding.OnboardingScreen
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: ProfileViewModel = viewModel { ProfileViewModel(AppModule.allAboardModel) }
        val uiState by viewModel.uiState.collectAsState()

        ProfileScreenContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onChangePreferences = { navigator?.push(OnboardingScreen(editMode = true)) },
            onLogOut = { /* TODO: figure out login/logout process */ }
        )
    }
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onChangePreferences: () -> Unit,
    onLogOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        ScreenTopBar(title = "My Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenTopBarDefaults.ContentPaddingHorizontal)
                .padding(top = 24.dp, bottom = 100.dp)
        ) {
            // Avatar + name
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display Profile Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(width = 2.dp, color = TextPrimary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        tint = TextPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = uiState.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )

                Spacer(Modifier.height(24.dp))
            }

            // Settings rows
            SettingsRow(text = "Change Preferences", onClick = onChangePreferences)
            Spacer(Modifier.height(24.dp))
            SettingsRow(text = "Log Out", onClick = onLogOut)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "", tint = TextSecondary)
    }
}
