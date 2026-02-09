package org.allaboard.project.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.screens.HomeScreen

/**
 * Single onboarding screen with 4 step components.
 * Step is UI-only state (remember); user data is in OnboardingViewModel.
 */
class OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val vm: OnboardingViewModel = viewModel()
        var step by remember { mutableIntStateOf(0) }

        when (step) {
            0 -> WelcomeStep(vm = vm, onNext = { step++ })
            1 -> TravelVibeStep(vm = vm, onNext = { step++ }, onBack = { step-- })
            2 -> BudgetStep(vm = vm, onNext = { step++ }, onBack = { step-- })
            3 -> InterestsStep(
                vm = vm,
                onFinish = { navigator?.push(HomeScreen()) },
                onBack = { step-- }
            )
            else -> WelcomeStep(vm = vm, onNext = { step++ })
        }
    }
}
