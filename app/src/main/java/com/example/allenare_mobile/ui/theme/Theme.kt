package com.example.allenare_mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFF0F0F0),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AllenaremobileTheme(
    // darkTheme: Boolean = isSystemInDarkTheme(), // Comentado para forzar el modo claro
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desactivado para un diseño consistente
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Siempre usamos el tema claro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}