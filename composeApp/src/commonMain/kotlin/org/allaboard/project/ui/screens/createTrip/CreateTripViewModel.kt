package org.allaboard.project.ui.screens.createTrip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class InviteStatus { Joined, Invited }

data class CrewMemberUi(
    val id: String,
    val name: String,
    val status: InviteStatus
)

data class CreateTripUiState(
    // Trip info (Step 0)
    val country: String = "",
    val region: String = "",
    val dateRange: String = "Date",
    val peopleCount: Int = 1,

    // Group info (Step 1)
    val crew: List<CrewMemberUi> = emptyList(),
    val inviteLink: String = "",

    val isCreatingTrip: Boolean = false
)

class CreateTripViewModel : ViewModel() {

    enum class Mode { Create, Edit }

    var uiState by mutableStateOf(CreateTripUiState())
        private set

    /**
     * Call once when CreateTripScreen loads.
     * In "Edit" mode you would load an existing trip by tripId.
     */
    fun initialize(mode: Mode, tripId: String?) {
        when (mode) {
            Mode.Create -> loadCreateDefaults()
            Mode.Edit -> {
                // TODO: load trip + group info from repository using tripId
                // For now: just load dummy data so UI has something to show
                loadEditDummy(tripId)
            }
        }
    }

    // Trip field updates (Step 0)
    fun updateCountry(v: String) {
        uiState = uiState.copy(country = v)
    }

    fun updateRegion(v: String) {
        uiState = uiState.copy(region = v)
    }

    fun updateDateRange(v: String) {
        uiState = uiState.copy(dateRange = v)
    }

    fun incPeople() {
        uiState = uiState.copy(peopleCount = uiState.peopleCount + 1)
    }

    fun decPeople() {
        val newCount = (uiState.peopleCount - 1).coerceAtLeast(1)
        uiState = uiState.copy(peopleCount = newCount)
    }

    // Group actions (Step 1)
    fun onAddFriend() {
        // TODO: open add-friend flow
        // For now: example behavior: add a fake invited member
        val nextId = (uiState.crew.size + 1).toString()
        val newMember = CrewMemberUi(
            id = nextId,
            name = "Friend $nextId",
            status = InviteStatus.Invited
        )
        uiState = uiState.copy(crew = uiState.crew + newMember)
    }

    fun onCopyLink() {
        // TODO: copy to clipboard
    }

    fun onCreateTrip() {
        // TODO: real create-trip call (API/repository)
        uiState = uiState.copy(isCreatingTrip = true)
    }

    // Helpers
    private fun loadCreateDefaults() {
        uiState = CreateTripUiState(
            country = "",
            region = "",
            dateRange = "Date",
            peopleCount = 1,
            crew = emptyList(),
            inviteLink = "",
            isCreatingTrip = false
        )
    }

    private fun loadEditDummy(tripId: String?) {
        uiState = CreateTripUiState(
            country = "Japan",
            region = "Tokyo",
            dateRange = "Dec 15 - Jan 22",
            peopleCount = 4,
            crew = listOf(
                CrewMemberUi(id = "1", name = "Sarah", status = InviteStatus.Joined),
                CrewMemberUi(id = "2", name = "Bethany", status = InviteStatus.Invited)
            ),
            inviteLink = "AllAboard.ca/join/JP-2026",
            isCreatingTrip = false
        )
    }
}

