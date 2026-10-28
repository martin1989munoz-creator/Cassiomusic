package com.example.casioplayer.ui

import androidx.compose.ui.graphics.Color
import com.example.casioplayer.data.CasioThemeMode

val CasioChassisBlack = Color(0xFF242824)
val CasioButtonColor = Color(0xFF394135)
val CasioButtonBorder = Color(0xFF1E221E)
val CasioAccentBlue = Color(0xFF1E3A8A)
val CasioAccentRed = Color(0xFF991B1B)

val LcdGreenBg = Color(0xFFB8C9A3)
val LcdGreenText = Color(0xFF273525)

val LcdAmberBg = Color(0xFFE5A93B)
val LcdAmberText = Color(0xFF3B2500)

val LcdMonoBg = Color(0xFFC8CDC5)
val LcdMonoText = Color(0xFF151815)

data class LcdColors(
    val background: Color,
    val onBackground: Color
)

fun getLcdColors(mode: CasioThemeMode): LcdColors {
    return when (mode) {
        CasioThemeMode.GREEN -> LcdColors(LcdGreenBg, LcdGreenText)
        CasioThemeMode.AMBER -> LcdColors(LcdAmberBg, LcdAmberText)
        CasioThemeMode.MONO -> LcdColors(LcdMonoBg, LcdMonoText)
    }
}