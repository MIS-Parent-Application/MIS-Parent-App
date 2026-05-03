package com.mis.parentapp.ui.theme

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

object ColorsDefaultTheme {
    // Green colors
    val color_Primary_green = Color(0xFF267D1E)
    val color_Primary_green_container = Color(0xFF215C18)
    val color_Primary_on_green = Color(0xFF7CA77A)
    
    // Yellow colors
    val color_Yellow = Color(0xFFDEF731)
    //val color_Yellow_container = Color(0xFFFAFD0B)
    val color_On_yellow = Color(0xFFE6EA85)

    // Error colors
    val color_Error = Color(0xFFB3261E)
    //val color_On_error = Color(0xFFFFFFFF)

    // Surface colors
    val color_Surface = Color(0xFFF6FDE7)
    val color_On_surface = Color(0xFF1C1B1F)
    val color_Outline = Color(0xFF79747E)

    val text_color = Color(0xFFFFFFFF)

    // Legacy references (kept for compatibility)
   // val color_Surface_surface = color_Surface
    val color_Surface_on_surface = color_On_surface
    //val color_Background_on_background = Color(0xFF1C1B1F)
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
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
