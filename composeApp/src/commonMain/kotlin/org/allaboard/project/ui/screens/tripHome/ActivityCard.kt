package org.allaboard.project.ui.screens.tripHome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary

/**
 * Reusable ActivityCard component that displays an activity with an image, title, and vote count
 *
 * @param title The title of the activity
 * @param voteCount The number of votes for this activity
 * @param modifier Optional modifier for the card
 * @param imageContent Optional composable for the image content (e.g., Image or placeholder)
 */
@Composable
fun ActivityCard(
    title: String,
    voteCount: Int,
    modifier: Modifier = Modifier,
    imageContent: @Composable () -> Unit = {
        // Placeholder box for image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
        )
    }
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image section with rounded top corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                imageContent()
            }

            // Content section (title and votes)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vote count
                Text(
                    text = "Total Votes: $voteCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
    }
}