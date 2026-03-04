package org.allaboard.project.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the NetworkImage component. Verifies the composable renders when given a URL and when
 * given null (placeholder/fallback); does not assert actual image loading behavior.
 */
internal class NetworkImageTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun networkImage_withUrl_renders() {
        rule.setContent {
            AppTheme {
                Box(Modifier.testTag("network_image_container")) {
                    NetworkImage(
                        imageUrl = "https://example.com/photo.jpg",
                        contentDescription = "Test",
                        modifier = Modifier.size(100.dp).testTag("network_image")
                    )
                }
            }
        }
        rule.onNodeWithTag("network_image").assertExists()
    }

    @Test
    fun networkImage_withNullUrl_renders() {
        rule.setContent {
            AppTheme {
                NetworkImage(
                    imageUrl = null,
                    contentDescription = "Placeholder",
                    modifier = Modifier.testTag("network_image_null")
                )
            }
        }
        rule.onNodeWithTag("network_image_null").assertExists()
    }
}
