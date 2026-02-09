package org.allaboard.project.ui.screens.createTrip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import org.allaboard.project.ui.theme.FieldBackground

@Composable
fun GroupSetupStep(
    vm: CreateTripViewModel,
    onBack: () -> Unit,
    onCreateTrip: () -> Unit
) {
    val state = vm.uiState

    val borderColor = Color.Black
    val cardShape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 45.dp,
                bottom = 40.dp
            )
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Group Setup",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Invite your group to plan together!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(30.dp))

            // Your Trip Crew
            CardContainer(
                title = "Your Trip Crew",
                borderColor = borderColor,
                shape = cardShape
            ) {
                if (state.crew.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No members yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Divider(color = borderColor, thickness = 1.dp)

                } else {
                    state.crew.forEachIndexed { i, member ->
                        CrewRow(member = member)
                        if (i != state.crew.lastIndex) {
                            Divider(color = borderColor, thickness = 1.dp)
                        }
                    }
                    Divider(color = borderColor, thickness = 1.dp)
                }

                // Add Friend row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.onAddFriend() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Friend",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEDEDED), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // Invite Friends
            CardContainer(
                title = "Invite Friends",
                borderColor = borderColor,
                shape = cardShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.inviteLink.ifBlank { "AllAboard.ca/123.." },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .height(35.dp)
                                .background(Color(0xFFEDEDED), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { vm.onCopyLink() },
                                contentPadding = PaddingValues(horizontal = 50.dp)
                            ) {
                                Text(
                                    text = "Copy Link",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FieldBackground,
                    contentColor = Color.Black
                )
            ) {
                Text("Back")
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    vm.onCreateTrip()
                    onCreateTrip()
                },
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
            ) {
                Text(
                    text = if (state.isCreatingTrip) "Creating..." else "Create trip",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun CardContainer(
    title: String,
    borderColor: Color,
    shape: RoundedCornerShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, shape)
    ) {
        // Title row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Divider(color = borderColor, thickness = 1.dp)

        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun CrewRow(member: CrewMemberUi) {
    val (statusText, statusBg, statusIcon) = when (member.status) {
        InviteStatus.Joined ->
            Triple("Joined", Color(0xFFD9FBE2), Icons.Outlined.Check)
        InviteStatus.Invited ->
            Triple("Invited", Color(0xFFF7F0C9), Icons.Outlined.Notifications)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .border(1.5.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Member avatar",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = member.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .background(statusBg, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )

                Spacer(Modifier.width(4.dp))

                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Black
                )
            }
        }
    }
}
