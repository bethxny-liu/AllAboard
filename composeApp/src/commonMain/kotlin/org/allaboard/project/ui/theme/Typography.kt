package org.allaboard.project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.fuzzy_bubbles_bold
import team_102_8.composeapp.generated.resources.fuzzy_bubbles_regular
import team_102_8.composeapp.generated.resources.geist_bold
import team_102_8.composeapp.generated.resources.geist_medium
import team_102_8.composeapp.generated.resources.geist_regular
import team_102_8.composeapp.generated.resources.geist_semibold

@Composable
fun appTypography(): Typography {

    val geistFontFamily = FontFamily(
        Font(Res.font.geist_regular, weight = FontWeight.Normal),
        Font(Res.font.geist_medium, weight = FontWeight.Medium),
        Font(Res.font.geist_semibold, weight = FontWeight.SemiBold),
        Font(Res.font.geist_bold, weight = FontWeight.Bold)
    )

    return Typography(
        headlineLarge = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = geistFontFamily
        ),
        headlineMedium = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = geistFontFamily
        ),
        titleMedium = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = geistFontFamily
        ),
        bodyLarge = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = geistFontFamily
        ),
        bodyMedium = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = geistFontFamily
        ),
        labelMedium = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = geistFontFamily
        )
    )
}

@Composable
fun fuzzyBubblesFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.fuzzy_bubbles_regular, weight = FontWeight.Normal),
        Font(Res.font.fuzzy_bubbles_bold, weight = FontWeight.Bold)
    )
}
