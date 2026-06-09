package com.yourcompany.pos.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PosColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonMint,
    tertiary = NeonPink,
    background = Background,
    surface = Surface,
    onPrimary = Background,
    onSecondary = Background,
    onTertiary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PosTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.statusBarColor = Background.toArgb()
            it.navigationBarColor = Background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = PosColorScheme,
        typography = PosTypography,
        content = content
    )
}
