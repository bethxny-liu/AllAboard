package org.allaboard.project.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.Surface as AppSurface
import org.allaboard.project.ui.theme.TextPrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person

enum class FooterItem { HOME, TRIPS, PROFILE }

@Composable
fun FooterNavBar(
    onHome: () -> Unit,
    onTrips: () -> Unit,
    onProfile: () -> Unit,
    activeItem: FooterItem,
    isTripActionExpanded: Boolean = false
) {
    val iconColor = TextPrimary
    val activePillColor = BluePrimary.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-20).dp)
            .padding(horizontal = 12.dp, vertical = 15.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(65.dp),
            shape = RoundedCornerShape(32.dp),
            color = AppSurface,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-108).dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeItem == FooterItem.HOME) activePillColor else Color.Transparent
                ) {
                    IconButton(
                        onClick = onHome,
                        modifier = Modifier.size(width = 116.dp, height = 44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(30.dp),
                            tint = iconColor
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 108.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeItem == FooterItem.PROFILE) activePillColor else Color.Transparent
                ) {
                    IconButton(
                        onClick = onProfile,
                        modifier = Modifier.size(width = 116.dp, height = 44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(30.dp),
                            tint = iconColor
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-5).dp)
                .size(72.dp),
            shape = CircleShape,
            color = BluePrimary,
            shadowElevation = 3.dp
        ) {
            IconButton(onClick = onTrips, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (isTripActionExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = "Add Trip",
                    modifier = Modifier.size(40.dp),
                    tint = TextPrimary
                )
            }
        }
    }
}
