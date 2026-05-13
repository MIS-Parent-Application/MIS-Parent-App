package com.mis.parentapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38B02D),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF143B0F),
    secondary = Color(0xFFE6FA5E),
    secondaryContainer = Color(0xFF454800),
    surface = Color(0xFF252529),
    onSurface = Color(0xFFE6E1E5),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    outline = Color(0xFF938F99),
    error = Color(0xFFF2B8B5)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF267D1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF215C18),
    secondary = Color(0xFFDEF731),
    secondaryContainer = Color(0xFFFAFD0B),
    surface = Color(0xFFF6FDE7),
    onSurface = Color(0xFF1C1B1F),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    outline = Color(0xFF79747E),
    error = Color(0xFFB3261E)
)

object ColorsDefaultTheme {
    // These will now dynamically resolve based on the current theme when used in Composables
    // Note: For objects like this to be truly dynamic, they should be accessed within a Composable context
    // or we can keep them as fixed constants if that's the intended "Default" behavior.
    // Given the previous implementation used fixed Colors, I will update them to the new Light brand colors 
    // as the "Default", but the app should ideally use MaterialTheme.colorScheme directly.
    
    val color_Primary_green = Color(0xFF267D1E)
    val color_Primary_green_container = Color(0xFF215C18)
    //val color_Primary_on_green = Color(0xFFFFFFFF)
    
    //val color_Yellow = Color(0xFFDEF731)
    val color_On_yellow = Color(0xFF1C1B1F)

    //val color_Error = Color(0xFFB3261E)

    val color_Surface = Color(0xFFF6FDE7)
    val color_On_surface = Color(0xFF1C1B1F)
    //val color_Outline = Color(0xFF79747E)

    val text_color = Color(0xFFFFFFFF)

    val color_Surface_on_surface = color_On_surface
}

object AppTypes {
    val type_H2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
    val type_Body_Small = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
    val type_H1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    )
    val type_Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    val type_M3_label_small = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
}

@Composable
fun ParentAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to prioritize custom theme colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // We only need to tell the system if the icons should be dark or light
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
