package org.allaboard.project.ui.screens.createTrip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.allaboard.project.domain.AllAboardModel
import org.allaboard.project.domain.Trip
import org.allaboard.project.domain.User

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
    val startDate: String = "",
    val endDate: String = "",
    val dateRange: String = "",
    val peopleCount: Int = 1,
    val tripId: String? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,

    // Group info (Step 1)
    val crew: List<CrewMemberUi> = emptyList(),
    val inviteLink: String = "",

    val isCreatingTrip: Boolean = false,
    val error: String? = null
)

class CreateTripViewModel(
    private val allAboardModel: AllAboardModel
) : ViewModel() {

    enum class Mode { Create, Edit }

    var uiState by mutableStateOf(CreateTripUiState())
        private set

    private var mode: Mode = Mode.Create

    /**
     * Call once when CreateTripScreen loads.
     * In "Edit" mode you would load an existing trip by tripId.
     */
    fun initialize(mode: Mode, tripId: String?) {
        this.mode = mode
        when (mode) {
            Mode.Create -> loadCreateDefaults()
            Mode.Edit -> {
                loadTripForEdit(tripId)
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
        val (startDate, endDate) = parseDateRange(v)
        uiState = uiState.copy(
            dateRange = v,
            startDate = startDate,
            endDate = endDate
        )
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
        onCreateTrip(onSuccess = {})
    }

    fun onCreateTrip(onSuccess: () -> Unit) {
        val state = uiState
        val destination = state.country.trim()
        if (destination.isBlank()) {
            uiState = state.copy(error = "Destination is required")
            return
        }

        val startDate = state.startDate.ifBlank { state.dateRange.ifBlank { "TBD" } }
        val endDate = state.endDate.ifBlank { startDate }
        val region = state.region.trim()

        uiState = state.copy(isCreatingTrip = true, error = null)

        viewModelScope.launch {
            try {
                when (mode) {
                    Mode.Create -> {
                        val currentUser = allAboardModel.getCurrentUser()
                            ?: error("No current user available")

                        val createdTrip = allAboardModel.createTrip(
                            destination = destination,
                            region = region,
                            startDate = startDate,
                            endDate = endDate,
                            creatorId = currentUser.id
                        )

                        uiState = uiState.copy(
                            tripId = createdTrip.id,
                            inviteLink = allAboardModel.getTripInviteLink(createdTrip.id),
                            isCreatingTrip = false
                        )
                    }

                    Mode.Edit -> {
                        val existingTripId = state.tripId ?: error("Missing trip id")
                        val updatedTrip = allAboardModel.updateTripDetails(
                            tripId = existingTripId,
                            destination = destination,
                            region = region,
                            startDate = startDate,
                            endDate = endDate
                        ) ?: error("Trip not found")

                        uiState = uiState.copy(
                            country = updatedTrip.destination,
                            region = updatedTrip.region,
                            startDate = updatedTrip.startDate,
                            endDate = updatedTrip.endDate,
                            dateRange = formatDateRange(updatedTrip.startDate, updatedTrip.endDate),
                            inviteLink = allAboardModel.getTripInviteLink(updatedTrip.id),
                            isCreatingTrip = false
                        )
                    }
                }
                onSuccess()
            } catch (t: Throwable) {
                uiState = uiState.copy(
                    isCreatingTrip = false,
                    error = t.message ?: "Unable to save trip"
                )
            }
        }
    }

    // Helpers
    private fun loadCreateDefaults() {
        uiState = uiState.copy(isLoading = true, error = null, isEditMode = false)
        viewModelScope.launch {
            val currentUser = allAboardModel.getCurrentUser()
            val crew = currentUser?.let { listOf(it.toCrewMemberUi(InviteStatus.Joined)) } ?: emptyList()
            uiState = CreateTripUiState(
                country = "",
                region = "",
                startDate = "",
                endDate = "",
                dateRange = "",
                peopleCount = crew.size.coerceAtLeast(1),
                tripId = null,
                isEditMode = false,
                isLoading = false,
                crew = crew,
                inviteLink = "",
                isCreatingTrip = false,
                error = null
            )
        }
    }

    private fun loadTripForEdit(tripId: String?) {
        if (tripId.isNullOrBlank()) {
            uiState = CreateTripUiState(
                isEditMode = true,
                error = "Missing trip id"
            )
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, isEditMode = true)
        viewModelScope.launch {
            try {
                val trip = allAboardModel.getTrip(tripId) ?: error("Trip not found")
                uiState = trip.toUiStateForEdit(
                    inviteLink = allAboardModel.getTripInviteLink(trip.id)
                )
            } catch (t: Throwable) {
                uiState = CreateTripUiState(
                    tripId = tripId,
                    isEditMode = true,
                    isLoading = false,
                    error = t.message ?: "Unable to load trip"
                )
            }
        }
    }

    private fun parseDateRange(value: String): Pair<String, String> {
        if (value.isBlank()) return "" to ""
        val parts = value.split(" - ", limit = 2)
        val start = parts.firstOrNull().orEmpty().trim()
        val end = parts.getOrNull(1)?.trim().orEmpty()
        return start to (end.ifBlank { start })
    }

    private fun Trip.toUiStateForEdit(inviteLink: String): CreateTripUiState {
        return CreateTripUiState(
            country = destination,
            region = region,
            startDate = startDate,
            endDate = endDate,
            dateRange = formatDateRange(startDate, endDate),
            peopleCount = memberCount.coerceAtLeast(1),
            tripId = id,
            isEditMode = true,
            isLoading = false,
            crew = members.map { it.toCrewMemberUi(InviteStatus.Joined) },
            inviteLink = inviteLink,
            isCreatingTrip = false,
            error = null
        )
    }

    private fun User.toCrewMemberUi(status: InviteStatus): CrewMemberUi {
        return CrewMemberUi(
            id = id,
            name = displayName,
            status = status
        )
    }

    private fun formatDateRange(startDate: String, endDate: String): String {
        if (startDate.isBlank()) return ""
        return if (endDate.isBlank() || endDate == startDate) startDate else "$startDate - $endDate"
    }
}
