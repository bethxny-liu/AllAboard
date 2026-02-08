package org.allaboard.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.screens.tripHome.TripHomeScreen
import org.allaboard.project.ui.screens.createTrip.CreateTripScreen

class HomeScreen : Screen {
    @Composable
    @Preview
    override fun Content() {
        val navigator = LocalNavigator.current

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Home Screen")

            Spacer(Modifier.height(16.dp))

            Button(onClick = { navigator?.push(TripHomeScreen())}) {
                Text("Trip Home")
            }

            Spacer(Modifier.height(8.dp))

            // Navigate to CreateTripScreen
            Button(
                onClick = { navigator?.push(CreateTripScreen()) },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE))
            ) {
                Text(
                    "+",
                    color = Color.Black,
                    fontSize = 20.sp,
                )
            }
        }
    }
}
