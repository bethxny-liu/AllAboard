package org.allaboard.project.ui.screens.activityDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.Activity
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.Surface
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.prettyplace
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary

/**
 * Activity/place details screen: app bar, hero image, title, location, rating, price,
 * description (with see more/less), and map section. Opened from trip home activity cards.
 */
class ActivityDetailsScreen(
    private val activity: Activity? = null,
    private val fallbackActivityId: String,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: ActivityDetailsViewModel = viewModel {
            ActivityDetailsViewModel(
                model = AppModule.allAboardModel,
                initialActivity = activity,
                activityId = fallbackActivityId
            )
        }
        val uiState by viewModel.uiState.collectAsState()


        ActivityDetailsContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onSeeMoreClick = viewModel::toggleDescriptionExpanded
        )
    }
}

private const val DESCRIPTION_SEE_MORE_THRESHOLD = 120

@Composable
private fun ActivityDetailsContent(
    uiState: ActivityDetailsUiState,
    onBack: () -> Unit,
    onSeeMoreClick: () -> Unit
) {
    val activity = uiState.activity
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(top = 40.dp)
    ) {
        ActivityDetailsTopBar(onBack = onBack)

        if (activity == null && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Unable to load details", color = TextSecondary)
            }
            return
        }

        if (activity != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                HeroImage()

                // Info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp, bottom = 32.dp)
                ) {
                    // Title row (title + stars)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.sizeIn(minWidth = 80.dp)
                        ) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Outlined.StarBorder,
                                    contentDescription = "Rating",
                                    modifier = Modifier.size(20.dp),
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Location row (location + price)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(18.dp),
                                tint = TextSecondary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = activity.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = activity.priceLevel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Description with "see more"
                    val descLines = if (uiState.descriptionExpanded) Int.MAX_VALUE else 4
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 22.sp,
                        maxLines = descLines,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!uiState.descriptionExpanded && activity.description.length > DESCRIPTION_SEE_MORE_THRESHOLD) {
                        Text(
                            text = "see more",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BluePrimary,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable(onClick = onSeeMoreClick)
                        )
                    } else if (uiState.descriptionExpanded) {
                        Text(
                            text = "see less",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BluePrimary,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable(onClick = onSeeMoreClick)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Map section
                    MapPlaceholder(pinLabel = activity.mapPinLabel)
                }
            }
        }
    }
}

@Composable
private fun ActivityDetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Activity Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun HeroImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.prettyplace),
            contentDescription = "Activity photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun MapPlaceholder(pinLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        // Map pin and label overlay
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Map pin",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFDC2626)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = pinLabel,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

