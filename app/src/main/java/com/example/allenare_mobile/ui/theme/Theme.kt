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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = AllenareBlue,
    onPrimary = Color.White,
    secondary = AllenareOrange,
    onSecondary = Color.White,
    tertiary = AllenareDark,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = AllenareDark,
    surface = SurfaceWhite,
    onSurface = AllenareDark,
    error = ErrorRed,
    onError = Color.White
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