package com.quest.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

// ─── Palette Quest : nature moderne ──────────────────────────────
// Blanc chaud, verts organiques, jaune soleil. Flat, arrondi, aéré.

// Base claire
val Cloud = Color(0xFFFBFCF7)        // fond — blanc cassé chaud
val Mist = Color(0xFFF0F5E9)         // surface vert brume
val Mint = Color(0xFFE2F6D5)         // surface accent menthe

// Verts
val Lime = Color(0xFF9FE870)         // action principale — lime vivant
val Leaf = Color(0xFF4C8C2B)         // vert feuille
val Forest = Color(0xFF163300)       // vert forêt — texte, contraste
val Moss = Color(0xFF5C7052)         // vert mousse — texte secondaire

// Jaunes
val Sun = Color(0xFFFFD75E)          // jaune soleil — accents chauds
val Honey = Color(0xFFE8A800)        // jaune miel — badges, highlights

// Sombre (thème nuit en sous-bois)
val NightForest = Color(0xFF101B0D)  // fond sombre
val NightMoss = Color(0xFF1B2A16)    // surface sombre
val NightMist = Color(0xFF25361E)    // surface sombre élevée

// Rareté des lieux
val RarityCommon = Color(0xFF8FA98B)     // sauge discrète
val RarityRare = Color(0xFF4C8C2B)       // vert feuille vif
val RarityLegendary = Color(0xFFE8A800)  // or soleil

private val LightColors = lightColorScheme(
    primary = Lime,
    onPrimary = Forest,
    primaryContainer = Mint,
    onPrimaryContainer = Forest,
    secondary = Sun,
    onSecondary = Forest,
    secondaryContainer = Color(0xFFFFF3CC),
    onSecondaryContainer = Color(0xFF4A3B00),
    tertiary = Leaf,
    onTertiary = Color.White,
    background = Cloud,
    onBackground = Forest,
    surface = Color.White,
    onSurface = Forest,
    surfaceVariant = Mist,
    onSurfaceVariant = Moss,
    outline = Color(0xFFD5DFC9),
    outlineVariant = Color(0xFFE6EDDC),
)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Forest,
    primaryContainer = NightMist,
    onPrimaryContainer = Lime,
    secondary = Sun,
    onSecondary = Forest,
    secondaryContainer = Color(0xFF3A3010),
    onSecondaryContainer = Sun,
    tertiary = Lime,
    onTertiary = Forest,
    background = NightForest,
    onBackground = Color(0xFFE8F2DE),
    surface = NightMoss,
    onSurface = Color(0xFFE8F2DE),
    surfaceVariant = NightMist,
    onSurfaceVariant = Color(0xFFA9BC9F),
    outline = Color(0xFF3A4A32),
    outlineVariant = Color(0xFF2C3A25),
)

// Formes organiques : tout est doux, rien d'anguleux
private val QuestShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

// Typo : titres affirmés, corps calme
private val QuestTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
        ),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun QuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = QuestShapes,
        typography = QuestTypography,
        content = content,
    )
}
