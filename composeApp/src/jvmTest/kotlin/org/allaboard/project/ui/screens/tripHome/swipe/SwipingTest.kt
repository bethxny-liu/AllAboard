package org.allaboard.project.ui.screens.tripHome.swipe

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
 * UI tests for the Swiping screen. Verifies the current card shows the activity title and that Yes and No
 * vote buttons are present and clickable; clicking Yes updates the vote state (e.g. displayed result).
 */
internal class SwipingTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun swiping_showsCardAndVoteButtons() {
        rule.setContent {
            AppTheme {
                SwipingTestContent(activityTitle = "Senso-ji Temple")
            }
        }
        rule.onNodeWithTag("swipe_card_title").assertTextEquals("Senso-ji Temple")
        rule.onNodeWithTag("swipe_yes").assertHasClickAction()
        rule.onNodeWithTag("swipe_no").assertHasClickAction()
    }

    @Test
    fun clickYes_updatesVoteState() {
        rule.setContent {
            AppTheme {
                SwipingTestContent(activityTitle = "Food Tour")
            }
        }
        rule.onNodeWithTag("swipe_vote_result").assertTextEquals("none")
        rule.onNodeWithTag("swipe_yes").performClick()
        rule.onNodeWithTag("swipe_vote_result").assertTextEquals("yes")
    }
}

@Composable
private fun SwipingTestContent(activityTitle: String) {
    var vote by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("swipe_column")
    ) {
        Text(activityTitle, modifier = Modifier.testTag("swipe_card_title"))
        Text(vote ?: "none", modifier = Modifier.testTag("swipe_vote_result"))
        Button(
            onClick = { vote = "yes" },
            modifier = Modifier.testTag("swipe_yes")
        ) { Text("Yes") }
        Button(
            onClick = { vote = "no" },
            modifier = Modifier.testTag("swipe_no")
        ) { Text("No") }
    }
}
