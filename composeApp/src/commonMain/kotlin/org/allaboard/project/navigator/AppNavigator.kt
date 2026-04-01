package org.allaboard.project.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.SlideTransition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Luggage
import cafe.adriel.voyager.core.screen.Screen
import org.allaboard.project.ui.screens.home.HomeScreen
import org.allaboard.project.ui.screens.login.LoginScreen
import org.allaboard.project.ui.screens.onboarding.OnboardingScreen
import org.allaboard.project.ui.screens.profile.ProfileScreen
import org.allaboard.project.ui.screens.createTrip.CreateTripScreen
import org.allaboard.project.ui.screens.joinTrip.JoinTripScreen
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.TextPrimary

fun Navigator.pushIfNotTop(screen: Screen) {
    val top = lastItem
    if (top::class != screen::class) {
        push(screen)
    }
}

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun AppNavigator() {
    Navigator(
        screen = LoginScreen(),
        disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
    ) { navigator ->
        var showTripActionMenu by rememberSaveable { mutableStateOf(false) }
        val currentItem = navigator.lastItemOrNull
        val activeItem = when (currentItem) {
            is HomeScreen -> FooterItem.HOME
            is ProfileScreen -> FooterItem.PROFILE
            else -> FooterItem.TRIPS // default to Home styling for now
        }
        val hideFooter = currentItem is LoginScreen || currentItem is OnboardingScreen

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                SlideTransition(
                    navigator = navigator,
                    disposeScreenAfterTransitionEnd = true
                )
            }

            if (!hideFooter) {
                FooterNavBar(
                    onHome = { if (activeItem != FooterItem.HOME) navigator.replace(HomeScreen()) },
                    onTrips = { showTripActionMenu = !showTripActionMenu },
                    onProfile = { if (activeItem != FooterItem.PROFILE) navigator.push(ProfileScreen()) },
                    activeItem = activeItem,
                    isTripActionExpanded = showTripActionMenu
                )
            }
        }

        if (showTripActionMenu) {
            val dismissInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = dismissInteraction,
                        indication = null
                    ) { showTripActionMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 130.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TripActionMenuItem(
                        label = "Create a trip",
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Create a trip",
                                tint = TextPrimary
                            )
                        },
                        onClick = {
                            showTripActionMenu = false
                            navigator.push(CreateTripScreen())
                        }
                    )
                    Spacer(modifier = Modifier.size(14.dp))
                    TripActionMenuItem(
                        label = "Join a trip",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Luggage,
                                contentDescription = "Join a trip",
                                tint = TextPrimary
                            )
                        },
                        onClick = {
                            showTripActionMenu = false
                            navigator.push(JoinTripScreen())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripActionMenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.size(10.dp))
            Surface(
                shape = CircleShape,
                color = BluePrimary.copy(alpha = 0.28f)
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    icon()
                }
            }
        }
    }
}
