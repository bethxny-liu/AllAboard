package org.allaboard.project.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.prettyplace

@Composable
fun HomeCard(
    trip: TripSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable { onClick() }
    ) {
        // Trip image
        Image(
            painter = painterResource(Res.drawable.prettyplace),
            contentDescription = trip.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )

        // Trip info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "👥 ${trip.memberCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Text(
                text = trip.dateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
