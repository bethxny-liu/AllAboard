package org.allaboard.project.ui.screens.createTrip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

class CreateTripScreen(
    private val mode: CreateTripViewModel.Mode = CreateTripViewModel.Mode.Create,
    private val tripId: String? = null,
    private val startStep: Int = 0
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val vm: CreateTripViewModel = viewModel()

        LaunchedEffect(mode, tripId) {
            vm.initialize(mode, tripId)
        }

        var step by rememberSaveable { mutableIntStateOf(startStep) }

        when (step) {
            0 -> InitialInfoStep(
                vm = vm,
                onBack = { navigator?.pop() },
                onNext = { step = 1 }
            )

            1 -> GroupSetupStep(
                vm = vm,
                onBack = { step = 0 },
                onCreateTrip = {
                    // later: navigate to TripHome or whatever screen after creating
                    // navigator?.replace(TripHomeScreen(...))
                }
            )
        }
    }
}
