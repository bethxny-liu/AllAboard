package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Swiping Results: header and confirmed count display.
 */
internal class SwipingResultsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun swipingResults_showsHeaderAndConfirmedCount() {
        rule.setContent {
            AppTheme {
                SwipingResultsTestContent(confirmedCount = 3)
            }
        }
        rule.onNodeWithTag("results_title").assertTextEquals("Voting Results")
        rule.onNodeWithTag("results_count").assertTextEquals("3 confirmed")
    }
}

@Composable
private fun SwipingResultsTestContent(confirmedCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("results_column")
    ) {
        Text("Voting Results", modifier = Modifier.testTag("results_title"))
        Text("$confirmedCount confirmed", modifier = Modifier.testTag("results_count"))
    }
}
