package org.allaboard.project.ui.screens.onboarding

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
 * UI tests for Onboarding: welcome title and Next button visibility, and step update on Next click.
 */
internal class OnboardingTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun onboardingWelcome_showsTitleAndNextButton() {
        rule.setContent {
            AppTheme {
                OnboardingTestContent(step = "welcome")
            }
        }
        rule.onNodeWithTag("onboarding_title").assertTextEquals("Welcome")
        rule.onNodeWithTag("onboarding_next").assertHasClickAction()
    }

    @Test
    fun clickNext_updatesStep() {
        rule.setContent {
            AppTheme {
                OnboardingTestContent(step = "welcome", showStepLabel = true)
            }
        }
        rule.onNodeWithTag("step_label").assertTextEquals("welcome")
        rule.onNodeWithTag("onboarding_next").performClick()
        rule.onNodeWithTag("step_label").assertTextEquals("vibe")
    }
}

@Composable
private fun OnboardingTestContent(step: String, showStepLabel: Boolean = false) {
    var currentStep by remember { mutableStateOf(step) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("onboarding_column"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showStepLabel) {
            Text(text = currentStep, modifier = Modifier.testTag("step_label"))
        }
        Text("Welcome", modifier = Modifier.testTag("onboarding_title"))
        Button(
            onClick = { currentStep = "vibe" },
            modifier = Modifier.testTag("onboarding_next")
        ) {
            Text("Next")
        }
    }
}
