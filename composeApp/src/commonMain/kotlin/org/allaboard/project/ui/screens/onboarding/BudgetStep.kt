package org.allaboard.project.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.allaboard.project.ui.theme.BluePrimary
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.fuzzyBubblesFontFamily
import org.allaboard.project.ui.theme.TextSecondary
import org.allaboard.project.domain.BudgetLevel
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.logo

@Composable
fun BudgetStep(
    uiState: OnboardingUiState,
    vm: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val currentBudget = uiState.budget
    var sliderPos by remember(currentBudget) {
        mutableFloatStateOf(
            when (currentBudget) {
                BudgetLevel.LOW -> 0f
                BudgetLevel.MEDIUM -> 1f
                BudgetLevel.HIGH -> 2f
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 45.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "All Aboard",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = fuzzyBubblesFontFamily()
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(
                "2 of 3",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What's your budget?",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This helps us suggest activities and stays",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BudgetSlider(
                    value = sliderPos,
                    onValueChange = { v -> sliderPos = v },
                    onValueChangeFinished = {
                        val step = sliderPos.roundToStep()
                        sliderPos = step.toFloat()
                        val level = when (step) {
                            0 -> BudgetLevel.LOW
                            1 -> BudgetLevel.MEDIUM
                            else -> BudgetLevel.HIGH
                        }
                        vm.updateBudget(level)
                    }
                )

                Spacer(Modifier.height(20.dp))

                BudgetDescriptions(selected = currentBudget)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FieldBackground,
                    contentColor = Color.Black
                )
            ) {
                Text("Back")
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
            ) {
                Text("Next", color = Color.Black)
            }
        }
    }
}

@Composable
private fun BudgetSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..2f,
            steps = 1,
            colors = SliderDefaults.colors(
                thumbColor = BluePrimary,
                activeTrackColor = BluePrimary,
                inactiveTrackColor = Color(0xFFBDBDBD)
            )
        )

        Spacer(Modifier.height(8.dp))

        val active = value.roundToStep()
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0..2).forEach { idx ->
                    val color = if (idx <= active) BluePrimary else Color(0xFFCBD5E1)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, shape = CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$", style = MaterialTheme.typography.titleMedium)
                Text("$$", style = MaterialTheme.typography.titleMedium)
                Text("$$$", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun BudgetDescriptions(selected: BudgetLevel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        @Composable
        fun entry(symbols: String, title: String, body: String, isSelected: Boolean) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "$symbols ($title)",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        entry(
            "$",
            "Budget",
            "You'll prioritize affordable stays, budget-friendly meals, and free or low-cost activities while still enjoying the experience.",
            selected == BudgetLevel.LOW
        )
        entry(
            "$$",
            "Mid-Range",
            "Expect well-rated hotels, a mix of casual and nice restaurants, and popular activities without overspending.",
            selected == BudgetLevel.MEDIUM
        )
        entry(
            "$$$",
            "Luxury",
            "Enjoy high-end accommodations, top-rated dining, private or exclusive experiences, and maximum comfort throughout your trip.",
            selected == BudgetLevel.HIGH
        )
    }
}

private fun Float.roundToStep(): Int = when {
    this < 0.5f -> 0
    this < 1.5f -> 1
    else -> 2
}
