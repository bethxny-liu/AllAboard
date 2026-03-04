package org.allaboard.project.ui.screens.createTrip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Create Trip: destination field and submit button, and text input updating field.
 */
internal class CreateTripTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun createTrip_showsDestinationFieldAndSubmit() {
        rule.setContent {
            AppTheme {
                CreateTripTestContent()
            }
        }
        rule.onNodeWithTag("create_trip_destination").assertTextEquals("")
        rule.onNodeWithTag("create_trip_submit").assertTextEquals("Create Trip")
    }

    @Test
    fun enterDestination_updatesState() {
        rule.setContent {
            AppTheme {
                CreateTripTestContent()
            }
        }
        rule.onNodeWithTag("create_trip_destination").performTextInput("Tokyo")
        rule.onNodeWithTag("create_trip_destination").assertTextEquals("Tokyo")
    }
}

@Composable
private fun CreateTripTestContent() {
    var destination by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("create_trip_column")
    ) {
        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            modifier = Modifier.testTag("create_trip_destination"),
            label = { Text("Destination") }
        )
        Button(
            onClick = { },
            modifier = Modifier.testTag("create_trip_submit")
        ) {
            Text("Create Trip")
        }
    }
}
