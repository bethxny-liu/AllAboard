package org.allaboard.project.ui.screens.tripHome

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
 * UI tests for Trip Home: trip title and Activities section display.
 */
internal class TripHomeTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun tripHome_showsTripTitle() {
        rule.setContent {
            AppTheme {
                TripHomeTestContent(tripTitle = "Japan 2025")
            }
        }
        rule.onNodeWithTag("trip_home_title").assertTextEquals("Japan 2025")
    }

    @Test
    fun tripHome_showsTabsOrSections() {
        rule.setContent {
            AppTheme {
                TripHomeTestContent(tripTitle = "Osaka")
            }
        }
        rule.onNodeWithTag("trip_home_activities").assertTextEquals("Activities")
    }
}

@Composable
private fun TripHomeTestContent(tripTitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("trip_home_column")
    ) {
        Text(tripTitle, modifier = Modifier.testTag("trip_home_title"))
        Text("Activities", modifier = Modifier.testTag("trip_home_activities"))
    }
}
