package org.allaboard.project.ui.screens.createTrip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.allaboard.project.ui.theme.FieldBackground
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialInfoStep(
    vm: CreateTripViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state = vm.uiState
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    val dateTextStyle = TextStyle(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp
    )

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

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create a trip",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Let's get started!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(30.dp))

        // Destination
        Text(
            "Destination",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(10.dp))
        // Country
        BasicTextField(
            value = vm.uiState.country,
            onValueChange = { vm.updateCountry(it) },
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(FieldBackground, RoundedCornerShape(25.dp))
                        .padding(start = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (vm.uiState.country.isEmpty()) {
                        Text(
                            "Country",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(Modifier.height(15.dp))
        BasicTextField(
            value = vm.uiState.region,
            onValueChange = { vm.updateRegion(it) },
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(FieldBackground, RoundedCornerShape(25.dp))
                        .padding(start = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (vm.uiState.region.isEmpty()) {
                        Text(
                            "Region",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(Modifier.height(30.dp))

        // Date
        Text(
            "Date",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(FieldBackground, RoundedCornerShape(25.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).padding(start = 20.dp)) {
                    BasicTextField(
                        value = vm.uiState.dateRange,
                        onValueChange = {},
                        singleLine = true,
                        readOnly = true,
                        textStyle = dateTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (vm.uiState.dateRange.isEmpty()) {
                        Text(
                            "Date",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(modifier = Modifier.padding(end = 12.dp)) {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = "Select dates"
                        )
                    }
                }

            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val formatted = formatDateRange(
                                dateRangePickerState.selectedStartDateMillis,
                                dateRangePickerState.selectedEndDateMillis
                            )
                            if (formatted.isNotEmpty()) {
                                vm.updateDateRange(formatted)
                            }
                            showDatePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
                ,
                properties = DialogProperties(usePlatformDefaultWidth = true)
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = true,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(360.dp)
                        .heightIn(max = 380.dp)
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        // People
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(FieldBackground, RoundedCornerShape(25.dp))
                .padding(start = 20.dp, end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("People",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,)

                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = { vm.decPeople() },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                    ) {
                        Text("−", fontSize = 20.sp)
                    }

                    Text(
                        text = vm.uiState.peopleCount.toString(),
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = { vm.incPeople() },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                    ) {
                        Text("+", fontSize = 20.sp)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Action buttons
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
                onClick = onNext,
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
            ) {
                Text("Next", color = Color.Black )
            }
        }
    }
}

private fun formatDateRange(startMillis: Long?, endMillis: Long?): String {
    if (startMillis == null) return ""
    val start = formatDate(startMillis)
    val end = endMillis?.let { formatDate(it) }
    return if (end == null) start else "$start - $end"
}

private fun formatDate(millis: Long): String {
    val date = Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return date.toString()
}
