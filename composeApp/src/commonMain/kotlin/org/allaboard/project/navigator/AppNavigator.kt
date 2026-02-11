package org.allaboard.project.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.SlideTransition
import org.allaboard.project.ui.screens.home.HomeScreen
import org.allaboard.project.ui.screens.login.LoginScreen
import org.allaboard.project.ui.screens.onboarding.OnboardingScreen

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun AppNavigator() {
    Navigator(
        screen = LoginScreen(),
        disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
    ) { navigator ->
        val currentItem = navigator.lastItemOrNull
        val activeItem = when (currentItem) {
            is HomeScreen -> FooterItem.HOME
            // TODO: Add cases for ProfileScreen when implemented
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
                    onTrips = { /* TODO: dummy */ },
                    onProfile = { /* TODO: dummy */ },
                    activeItem = activeItem
                )
            }
        }
    }
}
