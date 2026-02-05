package org.allaboard.project.ui

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.allaboard.project.navigator.AppNavigator
import org.allaboard.project.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        AppNavigator()
    }
}