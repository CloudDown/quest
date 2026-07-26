package com.quest.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Quest Clear ─────────────────────────────────────────────────
// Neutres pour la structure, un seul vert pour l’action.
// Objectif : hiérarchie lisible — on sait où regarder.

// Neutres (90 % de l’UI)
val Ink = Color(0xFF12141A)          // texte principal
val Slate = Color(0xFF5C6570)        // texte secondaire
val Paper = Color(0xFFF4F4F2)        // fond
val Snow = Color(0xFFFFFFFF)         // cartes / surfaces
val Line = Color(0xFFDDDDD8)         // séparateurs
val Soft = Color(0xFFECECE8)         // surface variante

// Action (CTA uniquement)
val Signal = Color(0xFF1B7A4A)       // vert franc, texte blanc
val SignalSoft = Color(0xFFDFF0E6)   // fond d’état positif

// Accent chaud (légendaire / célébration)
val Amber = Color(0xFFC47E00)
val AmberSoft = Color(0xFFFFF1D6)

// Sombre
val NightInk = Color(0xFFE8EAED)
val NightSlate = Color(0xFF9AA3AD)
val NightPaper = Color(0xFF0F1217)
val NightSnow = Color(0xFF1A1F27)
val NightSoft = Color(0xFF242B35)
val NightLine = Color(0xFF323A46)

// Rareté — langage distinct du brand
val RarityCommon = Color(0xFF8A9199)
val RarityRare = Color(0xFF1B7A4A)
val RarityLegendary = Color(0xFFC47E00)

// Alias (composants existants)
val Lime = Signal
val Leaf = Signal
val Forest = Ink
val Moss = Slate
val Cloud = Paper
val Mist = Soft
val Mint = SignalSoft
val Sun = Amber
val Honey = Amber
val NightForest = NightPaper
val NightMoss = NightSnow
val NightMist = NightSoft

private val LightColors = lightColorScheme(
    primary = Signal,
    onPrimary = Color.White,
    primaryContainer = SignalSoft,
    onPrimaryContainer = Color(0xFF0F3D26),
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = AmberSoft,
    onSecondaryContainer = Color(0xFF5C3A00),
    tertiary = Signal,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Snow,
    onSurface = Ink,
    surfaceVariant = Soft,
    onSurfaceVariant = Slate,
    outline = Line,
    outlineVariant = Soft,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3DCF7A),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF145C38),
    onPrimaryContainer = Color(0xFFB8F0D0),
    secondary = Color(0xFFE8B84A),
    onSecondary = Color(0xFF3D2A00),
    secondaryContainer = Color(0xFF4A3500),
    onSecondaryContainer = AmberSoft,
    tertiary = Color(0xFF3DCF7A),
    onTertiary = Color(0xFF00391F),
    background = NightPaper,
    onBackground = NightInk,
    surface = NightSnow,
    onSurface = NightInk,
    surfaceVariant = NightSoft,
    onSurfaceVariant = NightSlate,
    outline = NightLine,
    outlineVariant = NightSoft,
)

// Formes nettes : structure lisible, pas de « blob » organique
private val QuestShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private val QuestTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp,
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
        ),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(lineHeight = 24.sp),
        bodyMedium = base.bodyMedium.copy(lineHeight = 22.sp),
    )
}

@Composable
fun QuestTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = QuestShapes,
        typography = QuestTypography,
        content = content,
    )
}
