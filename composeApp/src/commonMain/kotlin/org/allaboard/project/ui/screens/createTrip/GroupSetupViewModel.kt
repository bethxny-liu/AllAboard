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

data class GroupSetupUiState(
    val crew: List<CrewMemberUi> = emptyList(),
    val inviteLink: String = "",
    val isCreatingTrip: Boolean = false
)

class GroupSetupViewModel : ViewModel() {

    var uiState by mutableStateOf(GroupSetupUiState())
        private set

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        uiState = GroupSetupUiState(
            crew = listOf(
                CrewMemberUi(
                    id = "1",
                    name = "Sarah",
                    status = InviteStatus.Joined
                ),
                CrewMemberUi(
                    id = "2",
                    name = "Bethany",
                    status = InviteStatus.Invited
                )
            ),

            inviteLink = "AllAboard.ca/join/JP-2026",

            isCreatingTrip = false
        )
    }

    fun setCrew(members: List<CrewMemberUi>) {
        uiState = uiState.copy(crew = members)
    }

    fun setInviteLink(link: String) {
        uiState = uiState.copy(inviteLink = link)
    }

    fun onAddFriend() {
        // TODO: open add-friend flow
    }

    fun onCopyLink() {
        // TODO: copy to clipboard
    }

    fun onCreateTrip() {
        // TODO: real create-trip call
        uiState = uiState.copy(isCreatingTrip = true)
    }
}
