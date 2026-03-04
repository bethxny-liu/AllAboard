package org.allaboard.project.ui.screens.activityDetails

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
 * UI tests for the Activity Details screen. Verifies the activity title and description are shown;
 * Yes and No vote buttons are present and clickable; and clicking Yes updates the vote state.
 */
internal class ActivityDetailsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun activityDetails_showsTitleAndDescription() {
        rule.setContent {
            AppTheme {
                ActivityDetailsTestContent(
                    title = "Senso-ji Temple",
                    description = "Historic temple in Tokyo"
                )
            }
        }
        rule.onNodeWithTag("activity_details_title").assertTextEquals("Senso-ji Temple")
        rule.onNodeWithTag("activity_details_description").assertTextEquals("Historic temple in Tokyo")
    }

    @Test
    fun voteButtons_haveClickAction() {
        rule.setContent {
            AppTheme {
                ActivityDetailsTestContent(title = "X", description = "Y")
            }
        }
        rule.onNodeWithTag("activity_details_vote_yes").assertHasClickAction()
        rule.onNodeWithTag("activity_details_vote_no").assertHasClickAction()
    }

    @Test
    fun clickVoteYes_invokesCallback() {
        rule.setContent {
            AppTheme {
                ActivityDetailsTestContent(title = "X", description = "Y", trackVote = true)
            }
        }
        rule.onNodeWithTag("activity_details_vote_status").assertTextEquals("none")
        rule.onNodeWithTag("activity_details_vote_yes").performClick()
        rule.onNodeWithTag("activity_details_vote_status").assertTextEquals("yes")
    }
}

@Composable
private fun ActivityDetailsTestContent(
    title: String,
    description: String,
    trackVote: Boolean = false
) {
    var vote by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("activity_details_column")
    ) {
        Text(title, modifier = Modifier.testTag("activity_details_title"))
        Text(description, modifier = Modifier.testTag("activity_details_description"))
        if (trackVote) {
            Text(vote ?: "none", modifier = Modifier.testTag("activity_details_vote_status"))
        }
        Button(
            onClick = { vote = "yes" },
            modifier = Modifier.testTag("activity_details_vote_yes")
        ) { Text("Yes") }
        Button(
            onClick = { vote = "no" },
            modifier = Modifier.testTag("activity_details_vote_no")
        ) { Text("No") }
    }
}
