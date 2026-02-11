package org.allaboard.project.navigator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.Surface
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.BluePrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.outlined.Send

enum class FooterItem { HOME, TRIPS, PROFILE }

@Composable
fun FooterNavBar(
    onHome: () -> Unit,
    onTrips: () -> Unit,
    onProfile: () -> Unit,
    activeItem: FooterItem
) {
    val inactiveColor = TextPrimary
    val homeColor = if (activeItem == FooterItem.HOME) BluePrimary else inactiveColor
    val tripsColor = if (activeItem == FooterItem.TRIPS) BluePrimary else inactiveColor
    val profileColor = if (activeItem == FooterItem.PROFILE) BluePrimary else inactiveColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onHome() }
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                tint = homeColor
            )
            Text(
                text = "Home",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = homeColor
            )
        }

        // Trips (dummy)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onTrips() }
        ) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = "Trips", tint = tripsColor)
            Text(
                text = "Trips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = tripsColor
            )
        }

        // Profile (dummy)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onProfile() }
        ) {
            Icon(imageVector = Icons.Filled.Person, contentDescription = "Profile", tint = profileColor)
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = profileColor
            )
        }
    }
}
