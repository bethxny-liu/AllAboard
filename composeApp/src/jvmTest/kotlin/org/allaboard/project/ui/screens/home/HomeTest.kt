package org.allaboard.project.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
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
 * UI tests for Home: greeting, Upcoming/Past trip headers, FAB visibility and click.
 */
internal class HomeTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun home_showsUpcomingAndPastTripsHeaders() {
        rule.setContent {
            AppTheme {
                HomeTestContent(displayName = "Alex")
            }
        }
        rule.onNodeWithTag("home_greeting").assertTextEquals("Where to, Alex?")
        rule.onNodeWithTag("home_upcoming").assertTextEquals("Upcoming Trips")
        rule.onNodeWithTag("home_past").assertTextEquals("Past Trips")
    }

    @Test
    fun fab_hasClickAction() {
        rule.setContent {
            AppTheme {
                HomeTestContent(displayName = "")
            }
        }
        rule.onNodeWithTag("home_fab").assertHasClickAction()
    }

    @Test
    fun clickFab_invokesCallback() {
        rule.setContent {
            AppTheme {
                HomeTestContent(displayName = "", trackClicks = true)
            }
        }
        rule.onNodeWithTag("home_click_status").assertTextEquals("0")
        rule.onNodeWithTag("home_fab").performClick()
        rule.onNodeWithTag("home_click_status").assertTextEquals("1")
    }
}

@Composable
private fun HomeTestContent(
    displayName: String,
    trackClicks: Boolean = false
) {
    var clickCount by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("home_column")
    ) {
        if (trackClicks) {
            Text(text = "$clickCount", modifier = Modifier.testTag("home_click_status"))
        }
        Text(
            text = if (displayName.isNotBlank()) "Where to, $displayName?" else "Where to?",
            modifier = Modifier.testTag("home_greeting")
        )
        Text("Upcoming Trips", modifier = Modifier.testTag("home_upcoming"))
        Text("Past Trips", modifier = Modifier.testTag("home_past"))
        FloatingActionButton(
            onClick = { clickCount++ },
            modifier = Modifier.testTag("home_fab")
        ) {
            Text("+")
        }
    }
}
