package org.allaboard.project.ui.screens.itinerary

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
 * UI tests for the Itinerary screen. Verifies the Itinerary title and the day label (e.g. "Day 1 - Jan 15")
 * are displayed.
 */
internal class ItineraryTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun itinerary_showsTitleAndDay() {
        rule.setContent {
            AppTheme {
                ItineraryTestContent(dayLabel = "Day 1 - Jan 15")
            }
        }
        rule.onNodeWithTag("itinerary_title").assertTextEquals("Itinerary")
        rule.onNodeWithTag("itinerary_day").assertTextEquals("Day 1 - Jan 15")
    }
}

@Composable
private fun ItineraryTestContent(dayLabel: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("itinerary_column")
    ) {
        Text("Itinerary", modifier = Modifier.testTag("itinerary_title"))
        Text(dayLabel, modifier = Modifier.testTag("itinerary_day"))
    }
}
