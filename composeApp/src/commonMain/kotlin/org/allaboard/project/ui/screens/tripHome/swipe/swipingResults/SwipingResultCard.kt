package org.allaboard.project.ui.screens.tripHome.swipe.swipingResults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.allaboard.project.domain.ActivityVoteResult
import org.allaboard.project.ui.components.NetworkImage
import org.allaboard.project.ui.theme.Success
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary

/**
 * Reusable card for the Swiping Results page. Shows activity image, name, vote count,
 * and the people who voted, with an optional "Most Votes" tag.
 *
 * @param result The activity vote result data (activity, votes, voters).
 * @param showMostVotesTag When true, shows a "Most Votes" badge (e.g. for the top result).
 * @param imageContent Optional custom image content; defaults to placeholder/prettyplace.
 * @param onClick Optional click handler; when set, tapping the card opens activity details.
 * @param modifier Optional modifier for the card.
 */
@Composable
fun SwipingResultCard(
    result: ActivityVoteResult,
    showMostVotesTag: Boolean = false,
    modifier: Modifier = Modifier,
    imageContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = onClick ?: {}
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image (left)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    if (imageContent != null) {
                        imageContent()
                    } else {
                        NetworkImage(
                            imageUrl = result.activity.imageUrl,
                            contentDescription = result.activity.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Content (right): name, votes, people
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = result.activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${result.yesVotes} votes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Voters",
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = voterDisplayText(result.voterNames),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (showMostVotesTag) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Success)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Most Votes",
                            style = MaterialTheme.typography.labelMedium,
                            color = Surface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/** Display text for voters, e.g. "Daniel, Rachael + 2 more" */
private fun voterDisplayText(voterNames: List<String>, maxVisible: Int = 2): String {
    if (voterNames.isEmpty()) return "No votes"
    val visible = voterNames.take(maxVisible)
    val extra = voterNames.size - visible.size
    return if (extra > 0) {
        "${visible.joinToString(", ")} + $extra more"
    } else {
        visible.joinToString(", ")
    }
}

