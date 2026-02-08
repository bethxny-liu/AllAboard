package org.allaboard.project.ui.screens.createTrip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateTripViewModel : ViewModel() {

    var country by mutableStateOf("")
        private set

    var region by mutableStateOf("")
        private set

    var dateRange by mutableStateOf("Date")
        private set

    var peopleCount by mutableStateOf(1)
        private set

    fun updateCountry(v: String) { country = v }
    fun updateRegion(v: String) { region = v }
    fun updateDateRange(v: String) { dateRange = v }

    fun incPeople() { peopleCount++ }
    fun decPeople() { if (peopleCount > 1) peopleCount-- }
}

