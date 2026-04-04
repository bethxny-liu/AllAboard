package org.allaboard.project.ui.screens.activityDetails

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.di.AppModule
import org.allaboard.project.domain.Activity
import org.allaboard.project.maps.googleMapsExternalUrl
import org.allaboard.project.maps.googleStaticMapImageUrl
import org.allaboard.project.maps.hasUsableMapCoordinates
import org.allaboard.project.maps.mapsStaticApiKey
import org.allaboard.project.navigator.pushIfNotTop
import org.allaboard.project.ui.components.NetworkImage
import org.allaboard.project.ui.components.ScreenTopBar
import org.allaboard.project.ui.components.TitleAlignment
import org.allaboard.project.ui.screens.createActivity.CreateCustomActivityScreen
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import org.allaboard.project.ui.theme.Warning
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Activity/place details screen: app bar, hero image, title, location, rating, price,
 * description (with see more/less), and map section. Opened from trip home activity cards.
 */
class ActivityDetailsScreen(
    private val tripId: String,
    private val activity: Activity? = null,
    private val fallbackActivityId: String,
) : Screen {
    override val key = super.key + "${Random.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE)}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: ActivityDetailsViewModel = viewModel {
            ActivityDetailsViewModel(
                model = AppModule.allAboardModel,
                tripId = tripId,
                initialActivity = activity,
                activityId = fallbackActivityId
            )
        }
        val uiState by viewModel.uiState.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.activityDeleted) {
            if (uiState.activityDeleted) navigator?.pop()
        }

        ActivityDetailsContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onSeeMoreClick = viewModel::toggleDescriptionExpanded,
            onEditClick = {
                uiState.activity?.let { act ->
                    navigator?.pushIfNotTop(
                        CreateCustomActivityScreen(tripId = tripId, existingActivity = act)
                    )
                }
            },
            onDeleteClick = { showDeleteDialog = true }
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete activity?") },
                text = { Text("This activity will be permanently removed from the trip.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteActivity()
                        showDeleteDialog = false
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }
    }
}

private const val DESCRIPTION_SEE_MORE_THRESHOLD = 120

/** One decimal place; KMP-safe (avoid JVM-only [String.format]). */
private fun formatRatingOutOfFive(rating: Float): String {
    val r = (rating * 10f).roundToInt() / 10f
    val whole = r.toInt()
    val tenth = ((r - whole) * 10f).roundToInt().coerceIn(0, 9)
    return "$whole.$tenth / 5"
}

@Composable
private fun ActivityDetailsContent(
    uiState: ActivityDetailsUiState,
    onBack: () -> Unit,
    onSeeMoreClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val activity = uiState.activity
    var menuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        ScreenTopBar(
            title = "Activity Details",
            onBack = onBack,
            titleAlignment = TitleAlignment.Left,
            trailingContent = if (activity != null) {
                {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More options",
                                tint = TextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = TextPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = TextPrimary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red)
                                }
                            )
                        }
                    }
                }
            } else null
        )

        when {
            activity == null && !uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Unable to load details", color = TextSecondary)
                }
            }
            activity != null -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    HeroImage(imageUrl = activity.imageUrl)

                    // Info section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 32.dp, bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Warning
                                )
                                Text(
                                    text = formatRatingOutOfFive(activity.rating.coerceIn(0f, 5f)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
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
                        val desc = activity.description ?: ""
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 22.sp,
                            maxLines = descLines,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!uiState.descriptionExpanded && desc.length > DESCRIPTION_SEE_MORE_THRESHOLD) {
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

                        // Map section (Static Maps API preview; tap opens Google Maps)
                        ActivityLocationMap(
                            pinLabel = activity.mapPinDisplay,
                            latitude = activity.latitude,
                            longitude = activity.longitude,
                            locationText = activity.location
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroImage(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        NetworkImage(
            imageUrl = imageUrl,
            contentDescription = "Activity photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ActivityLocationMap(
    pinLabel: String,
    latitude: Double?,
    longitude: Double?,
    locationText: String,
) {
    val uriHandler = LocalUriHandler.current
    val coordsOk = hasUsableMapCoordinates(latitude, longitude)
    val apiKey = remember { mapsStaticApiKey() }
    val staticMapUrl = remember(latitude, longitude, apiKey, coordsOk) {
        if (!coordsOk) null
        else googleStaticMapImageUrl(latitude!!, longitude!!, apiKey)
    }
    val openInMapsUrl = remember(latitude, longitude, locationText) {
        googleMapsExternalUrl(latitude, longitude, locationText)
    }
    val mapModifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .clip(RoundedCornerShape(16.dp))
    val openDescription = "Open location in Google Maps"
    val baseModifier = if (openInMapsUrl != null) {
        mapModifier
            .semantics {
                contentDescription = openDescription
                role = Role.Button
            }
            .clickable { uriHandler.openUri(openInMapsUrl) }
    } else {
        mapModifier
    }

    Box(modifier = baseModifier.background(Color(0xFFE5E7EB))) {
        when {
            staticMapUrl != null -> {
                NetworkImage(
                    imageUrl = staticMapUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            !coordsOk -> {
                Text(
                    text = "No location found",
                    modifier = Modifier.align(Alignment.Center).padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            else -> {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
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
    }
}

