package org.allaboard.project.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.*

class BudgetScreen : Screen {
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        // default to MEDIUM so preview shows the middle thumb like the mock
        var selected by remember { mutableStateOf(BudgetLevel.MEDIUM) }
        var sliderPos by remember { mutableStateOf(1f) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
            // App bar (logo + title, no back on top)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(Res.drawable.logo), contentDescription = null, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(8.dp))
                Text("All Aboard", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text("2 of 3", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }

            Spacer(Modifier.height(12.dp))

            Text("What's your budget?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)

            Spacer(Modifier.height(16.dp))

            // Centered content: slider + descriptions
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.92f), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Slider block
                    BudgetSlider(
                        value = sliderPos,
                        onValueChange = { v ->
                            sliderPos = v
                        },
                        onValueChangeFinished = {
                            // snap to nearest step
                            val step = sliderPos.roundToStep()
                            sliderPos = step.toFloat()
                            selected = when (step) {
                                0 -> BudgetLevel.LOW
                                1 -> BudgetLevel.MEDIUM
                                else -> BudgetLevel.HIGH
                            }
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Descriptions: show detailed entries similar to Figma with the current section emphasized
                    BudgetDescriptions(selected = selected)
                }
            }

            // Bottom nav: Back (left) and Next (right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BackPill(onClick = { navigator?.pop() }, modifier = Modifier.weight(1f).height(56.dp))

                Button(
                    onClick = { navigator?.push(InterestsScreen()) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = TextPrimary),
                    enabled = true
                ) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun BudgetSlider(value: Float, onValueChange: (Float) -> Unit, onValueChangeFinished: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Slider with 3 discrete steps: 0,1,2
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

        // Tick markers and labels under the slider
        val active = value.roundToStep()
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                (0..2).forEach { idx ->
                    val color = if (idx <= active) BluePrimary else Color(0xFFCBD5E1)
                    Box(modifier = Modifier.size(8.dp).background(color, shape = CircleShape))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        // Each block: heading + body; highlight the selected one with primary color for heading
        @Composable
        fun entry(symbols: String, title: String, body: String, isSelected: Boolean) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "$symbols ($title)",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) BluePrimary else TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        entry("$", "Budget", "You'll prioritize affordable stays, budget-friendly meals, and free or low-cost activities while still enjoying the experience.", selected == BudgetLevel.LOW)
        entry("$$", "Mid-Range", "Expect well-rated hotels, a mix of casual and nice restaurants, and popular activities without overspending.", selected == BudgetLevel.MEDIUM)
        entry("$$$", "Luxury", "Enjoy high-end accommodations, top-rated dining, private or exclusive experiences, and maximum comfort throughout your trip.", selected == BudgetLevel.HIGH)
    }
}

private fun Float.roundToStep(): Int = when {
    this < 0.5f -> 0
    this < 1.5f -> 1
    else -> 2
}

@Preview(name = "BudgetMobile", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun BudgetPreview() {
    AppTheme { BudgetScreen().Content() }
}
