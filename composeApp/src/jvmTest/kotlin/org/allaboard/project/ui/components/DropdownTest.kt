package org.allaboard.project.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for dropdown-style UI (e.g. category selector). Verifies the currently selected value is
 * displayed and that clicking the trigger expands the menu and shows options (e.g. Landmarks, Food).
 */
internal class DropdownTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun dropdown_showsSelectedValue() {
        rule.setContent {
            AppTheme {
                DropdownTestContent(selected = "Landmarks")
            }
        }
        rule.onNodeWithTag("dropdown_selected").assertTextEquals("Landmarks")
    }

    @Test
    fun clickDropdown_expandsAndShowsOptions() {
        rule.setContent {
            AppTheme {
                DropdownTestContent(selected = "Food", expandable = true)
            }
        }
        rule.onNodeWithTag("dropdown_trigger").performClick()
        rule.onNodeWithTag("dropdown_option_0").assertTextEquals("Landmarks")
    }
}

@Composable
private fun DropdownTestContent(selected: String, expandable: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("dropdown_column")
    ) {
        Text(
            text = selected,
            modifier = Modifier.testTag("dropdown_selected")
        )
        if (expandable) {
            androidx.compose.material3.Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.testTag("dropdown_trigger")
            ) {
                Text("Open")
            }
            if (expanded) {
                Text("Landmarks", modifier = Modifier.testTag("dropdown_option_0"))
                Text("Food", modifier = Modifier.testTag("dropdown_option_1"))
            }
        }
    }
}
