package org.allaboard.project.navigator

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.SlideTransition
import org.allaboard.project.ui.screens.login.LoginScreen

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun AppNavigator() {
    Navigator(
        screen = LoginScreen(),
    disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
    ) { navigator ->
        SlideTransition(
            navigator = navigator,
            disposeScreenAfterTransitionEnd = true
        )
    }
}


