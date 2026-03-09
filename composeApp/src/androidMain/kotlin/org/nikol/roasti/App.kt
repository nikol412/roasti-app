package org.nikol.roasti

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.nikol.roasti.navigation.AppNavHost
import org.nikol.roasti.ui.theme.RoastiTheme

@Composable
fun App() {
    RoastiTheme {
        AppNavHost()
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
