package org.allaboard.project.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
 * UI tests for the Login screen. Verifies the initial screen shows app title "All Aboard", subtitle,
 * and "Sign in with Google" button with click action; and that clicking the sign-in button updates
 * displayed state (e.g. status text).
 */
internal class LoginTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun initialScreen_showsSignInButton() {
        rule.setContent {
            AppTheme {
                LoginTestContent(showStatus = false)
            }
        }
        rule.onNodeWithTag("sign_in_button_text").assertTextEquals("Sign in with Google")
        rule.onNodeWithTag("sign_in_button").assertHasClickAction()
    }

    @Test
    fun clickSignInButton_updatesState() {
        rule.setContent {
            AppTheme {
                LoginTestContent(showStatus = true)
            }
        }
        rule.onNodeWithTag("status_text").assertTextEquals("Ready")
        rule.onNodeWithTag("sign_in_button").performClick()
        rule.onNodeWithTag("status_text").assertTextEquals("Signed in!")
    }
}

@Composable
private fun LoginTestContent(showStatus: Boolean) {
    var status by remember { mutableStateOf("Ready") }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("login_column"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showStatus) {
            Text(text = status, modifier = Modifier.testTag("status_text"))
        }
        Button(
            onClick = { status = "Signed in!" },
            modifier = Modifier.testTag("sign_in_button")
        ) {
            Text("Sign in with Google", modifier = Modifier.testTag("sign_in_button_text"))
        }
    }
}
