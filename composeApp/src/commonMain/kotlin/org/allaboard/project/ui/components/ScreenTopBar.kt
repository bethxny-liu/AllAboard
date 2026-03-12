package org.allaboard.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary

/** Standard top bar for secondary screens: back button, title, consistent padding and typography. */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    titleAlignment: TitleAlignment = TitleAlignment.Center,
    modifier: Modifier = Modifier
) {
    val barModifier = modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .background(Surface)
        .padding(
            top = ScreenTopBarDefaults.PaddingTop,
            start = ScreenTopBarDefaults.PaddingHorizontal,
            end = ScreenTopBarDefaults.PaddingHorizontal,
            bottom = ScreenTopBarDefaults.PaddingVertical
        )

    when (titleAlignment) {
        TitleAlignment.Left -> {
            Row(
                modifier = barModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(Modifier.width(ScreenTopBarDefaults.SpacerWidth))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        TitleAlignment.Center -> {
            Box(
                modifier = barModifier,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

enum class TitleAlignment {
    /** Back button + title in a row, left-aligned (default). */
    Left,
    /** Title centered; back button remains on the left. */
    Center
}

object ScreenTopBarDefaults {
    val PaddingTop = 40.dp
    val PaddingVertical = 16.dp
    val PaddingHorizontal = 8.dp
    val SpacerWidth = 8.dp

    /** Use this for the main content area below the top bar (horizontal). */
    val ContentPaddingHorizontal = 24.dp
}
