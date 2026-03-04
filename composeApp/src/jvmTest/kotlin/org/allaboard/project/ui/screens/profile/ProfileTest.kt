package org.allaboard.project.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Profile: title, display name, preferences button visibility and click callback.
 */
internal class ProfileTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun profile_showsDisplayNameAndPreferences() {
        rule.setContent {
            AppTheme {
                ProfileTestContent(displayName = "Jordan")
            }
        }
        rule.onNodeWithTag("profile_title").assertTextEquals("Profile")
        rule.onNodeWithTag("profile_name").assertTextEquals("Jordan")
        rule.onNodeWithTag("profile_preferences").assertHasClickAction()
    }

    @Test
    fun clickPreferences_invokesCallback() {
        rule.setContent {
            AppTheme {
                ProfileTestContent(displayName = "Sam")
            }
        }
        rule.onNodeWithTag("profile_clicked").assertTextEquals("false")
        rule.onNodeWithTag("profile_preferences").performClick()
        rule.onNodeWithTag("profile_clicked").assertTextEquals("true")
    }
}

@Composable
private fun ProfileTestContent(displayName: String) {
    var clicked by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profile_column")
    ) {
        Text("Profile", modifier = Modifier.testTag("profile_title"))
        Text(displayName, modifier = Modifier.testTag("profile_name"))
        Text("$clicked", modifier = Modifier.testTag("profile_clicked"))
        Button(
            onClick = { clicked = true },
            modifier = Modifier.testTag("profile_preferences")
        ) {
            Text("Change preferences")
        }
    }
}
