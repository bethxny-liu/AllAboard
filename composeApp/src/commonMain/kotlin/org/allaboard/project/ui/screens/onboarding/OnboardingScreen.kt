package org.allaboard.project.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.di.AppModule
import org.allaboard.project.ui.screens.home.HomeScreen

/**
 * Single onboarding screen with 4 step components.
 * Step is UI-only state (remember); user data is in OnboardingViewModel.
 */
class OnboardingScreen (private val editMode: Boolean = false): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val vm: OnboardingViewModel = viewModel {
            OnboardingViewModel(model = AppModule.allAboardModel, editMode = editMode)
        }
        val uiState by vm.uiState.collectAsState()
        var step by remember { mutableIntStateOf(if (editMode) 1 else 0) }

        when (step) {
            0 -> WelcomeStep(onNext = { step++ })
            1 -> TravelVibeStep(uiState = uiState, vm = vm, onNext = { step++ }, onBack = { step-- })
            2 -> BudgetStep(uiState = uiState, vm = vm, onNext = { step++ }, onBack = { step-- })
            3 -> InterestsStep(
                uiState = uiState,
                vm = vm,
                onFinish = {
                    vm.savePreferences {
                        navigator?.replace(HomeScreen())
                    }
                },
                onBack = { step-- }
            )
            else -> WelcomeStep(onNext = { step++ })
        }
    }
}
