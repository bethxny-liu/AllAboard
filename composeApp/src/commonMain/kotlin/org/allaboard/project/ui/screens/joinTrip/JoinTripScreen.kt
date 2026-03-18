package org.allaboard.project.ui.screens.joinTrip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch
import org.allaboard.project.di.AppModule
import org.allaboard.project.ui.screens.tripHome.TripHomeScreen
import org.allaboard.project.ui.theme.Background
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.Error
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.TextHint

class JoinTripScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()
        var code by rememberSaveable { mutableStateOf("") }
        var isLoading by rememberSaveable { mutableStateOf(false) }
        var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(start = 24.dp, end = 24.dp, top = 45.dp, bottom = 20.dp)
        ) {
            IconButton(
                onClick = { navigator?.pop() },
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(220.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join a trip",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Let’s get started!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            BasicTextField(
                value = code,
                onValueChange = {
                    code = it
                    errorMessage = null
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(FieldBackground, RoundedCornerShape(27.dp))
                            .padding(horizontal = 24.dp),
                    ) {
                        if (code.isBlank()) {
                            Text(
                                text = "Code",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextHint,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        Box(modifier = Modifier.align(Alignment.CenterStart)) {
                            innerTextField()
                        }
                    }
                }
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navigator?.pop() },
                    modifier = Modifier
                        .height(50.dp)
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
                        if (isLoading) return@Button
                        val tripId = parseTripId(code)
                        if (tripId.isBlank()) {
                            errorMessage = "Enter a valid trip code."
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            val joined = runCatching { AppModule.allAboardModel.joinTrip(tripId) }.isSuccess
                            val trip = AppModule.allAboardModel.getTrip(tripId)
                            isLoading = false
                            if (trip != null) {
                                navigator?.push(TripHomeScreen(trip.id))
                            } else {
                                errorMessage = if (joined) {
                                    "Unable to open this trip right now."
                                } else {
                                    "No trip exists for that code."
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f),
                    enabled = code.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        disabledContainerColor = BluePrimary.copy(alpha = 0.4f),
                        contentColor = Color.Black,
                        disabledContentColor = Color.Black.copy(alpha = 0.6f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Next", color = Color.Black)
                    }
                }
            }
        }
    }
}

private fun parseTripId(raw: String): String {
    val value = raw.trim()
    if (value.isBlank()) return ""
    if (!value.contains("/")) return value
    return value.substringAfterLast("/").substringBefore("?").trim()
}
