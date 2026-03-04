package org.allaboard.project.ui.screens.createActivity

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
 * UI tests for Create Activity: title field and submit button, and text input updating field.
 */
internal class CreateActivityTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun createActivity_showsTitleFieldAndSubmit() {
        rule.setContent {
            AppTheme {
                CreateActivityTestContent()
            }
        }
        rule.onNodeWithTag("create_activity_title").assertTextEquals("")
        rule.onNodeWithTag("create_activity_submit").assertTextEquals("Add Activity")
    }

    @Test
    fun enterTitle_updatesState() {
        rule.setContent {
            AppTheme {
                CreateActivityTestContent()
            }
        }
        rule.onNodeWithTag("create_activity_title").performTextInput("Temple Visit")
        rule.onNodeWithTag("create_activity_title").assertTextEquals("Temple Visit")
    }
}

@Composable
private fun CreateActivityTestContent() {
    var title by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("create_activity_column")
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.testTag("create_activity_title"),
            label = { Text("Activity title") }
        )
        Button(
            onClick = { },
            modifier = Modifier.testTag("create_activity_submit")
        ) {
            Text("Add Activity")
        }
    }
}
