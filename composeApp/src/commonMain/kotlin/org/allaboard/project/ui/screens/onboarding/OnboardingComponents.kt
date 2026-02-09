package org.allaboard.project.ui.screens.onboarding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import org.allaboard.project.ui.theme.*
import androidx.compose.ui.graphics.painter.Painter

// Simple card used on the TravelVibe screen.
@Composable
fun VibeCard(
    iconPainter: Painter,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) BluePrimaryDark else Color(0xFFE5E7EB)
    val backgroundColor = if (selected) Color(0xFFF8FEFF) else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background square with gentle rounded corners
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3FAFF)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp), color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280), fontSize = 13.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Selection indicator: outer circle and inner filled when selected
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) BluePrimary else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                if (!selected) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(6.dp)).background(Color.White))
                }
            }
        }
    }
}

@Composable
fun InterestChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) BluePrimary else Color.White
    val borderColor = if (selected) BluePrimary else Color(0xFFCBD5E1)

    Box(
        modifier = Modifier
            .padding(6.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else TextPrimary)
    }
}

// Reusable Back pill button used across onboarding screens. Adjusted appearance for better preview visibility.
@Composable
fun BackPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 72.dp)
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECEFF1), contentColor = TextPrimary)
    ) {
        Text("Back", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

// Shared enum for budget levels used by BudgetStep
enum class BudgetLevel { LOW, MEDIUM, HIGH }
