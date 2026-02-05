package org.allaboard.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.ui.screens.login.LoginScreen
import org.allaboard.project.ui.screens.tripHome.TripHomeScreen

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
        }
    }
}
