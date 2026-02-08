package com.nolly.surecharge.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light
private val LightPrimary = Color(0xFFF4A7B9)
private val LightOnPrimary = Color(0xFF1F2937)
private val LightBackground = Color(0xFFF4F4F5)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF09090B)
private val LightOutline = Color(0xFFE4E4E7)
private val LightColorScheme = lightColorScheme(
	primary = LightPrimary,
	onPrimary = LightOnPrimary,
	secondary = LightPrimary,
	onSecondary = LightOnPrimary,
	background = LightBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	outline = LightOutline
)

// Dark
private val DarkPrimary = Color(0xFFF9A8D4)
private val DarkOnPrimary = Color(0xFF020617)
private val DarkBackground = Color(0xFF020617)
private val DarkSurface = Color(0xFF09090B)
private val DarkOnSurface = Color(0xFFE4E4E7)
private val DarkOutline = Color(0xFF27272A)
private val DarkColorScheme = darkColorScheme(
	primary = DarkPrimary,
	onPrimary = DarkOnPrimary,
	secondary = DarkPrimary,
	onSecondary = DarkOnPrimary,
	background = DarkBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	outline = DarkOutline
)

@Composable
fun SureChargeTheme(
	useDarkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
	val view = LocalView.current

	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as Activity).window
			WindowCompat.setDecorFitsSystemWindows(window, false)
		}
	}

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography(),
		shapes = Shapes(
			extraSmall = RoundedCornerShape(8),
			small = RoundedCornerShape(10),
			medium = RoundedCornerShape(14),
			large = RoundedCornerShape(18),
			extraLarge = RoundedCornerShape(24)
		),
		content = content
	)
}
