package org.allaboard.project.ui.screens.createTrip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import org.allaboard.project.ui.theme.FieldBackground

@Composable
fun GroupSetupStep(
    vm: CreateTripViewModel,
    onBack: () -> Unit,
    onCreateTrip: () -> Unit
) {
    val state = vm.uiState
    val clipboardManager = LocalClipboardManager.current

    val borderColor = Color.Black
    val cardShape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 45.dp,
                bottom = 20.dp
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
                    style = MaterialTheme.typography.headlineLarge,
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
                } else {
                    state.crew.forEachIndexed { i, member ->
                        CrewRow(
                            member = member,
                            canKick = state.tripId != null && member.id != state.currentUserId,
                            onKick = { vm.onKickMember(member.id) }
                        )
                        if (i != state.crew.lastIndex) {
                            Divider(color = borderColor, thickness = 1.dp)
                        }
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
                        val tripIdText = state.tripId
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                            ?: state.inviteLink
                                .substringAfterLast("/")
                                .substringBefore("?")
                                .trim()
                                .ifBlank { "" }
                        val hasTripCode = tripIdText.isNotBlank()
                        Text(
                            text = if (hasTripCode) tripIdText else "Create trip to generate code",
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
                                onClick = {
                                    vm.onCopyLink { link ->
                                        clipboardManager.setText(AnnotatedString(link))
                                    }
                                },
                                enabled = hasTripCode,
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
                    vm.onCreateTrip(onSuccess = onCreateTrip)
                },
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                enabled = !state.isCreatingTrip && !state.isLoading
            ) {
                Text(
                    text = when {
                        state.isCreatingTrip -> if (state.isEditMode) "Saving..." else "Creating..."
                        state.isEditMode -> "Save trip"
                        else -> "Create trip"
                    },
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
private fun CrewRow(
    member: CrewMemberUi,
    canKick: Boolean,
    onKick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
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

        if (canKick) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Member options",
                        tint = Color.Black
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Kick out", color = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            onKick()
                        }
                    )
                }
            }
        }
    }
}
