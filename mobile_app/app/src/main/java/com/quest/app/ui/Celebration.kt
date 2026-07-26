package com.quest.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val PARTICLE_EMOJIS = listOf("🌿", "☀️", "🌻", "🍃", "✨", "🌱")

private data class Particle(
    val emoji: String,
    val angleRad: Double,
    val distance: Float,
    val rotations: Float,
    val scale: Float,
)

/**
 * Carte de félicitations avec explosion de feuilles et de soleil.
 * L'animation se joue une fois à l'apparition.
 */
@Composable
fun ValidatedCelebration() {
    val particles = remember {
        List(14) { i ->
            val random = Random(i)
            Particle(
                emoji = PARTICLE_EMOJIS[i % PARTICLE_EMOJIS.size],
                angleRad = random.nextDouble(-Math.PI, 0.0), // vers le haut
                distance = random.nextInt(90, 220).toFloat(),
                rotations = random.nextInt(-2, 3).toFloat(),
                scale = 0.7f + random.nextFloat() * 0.8f,
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1400, easing = FastOutSlowInEasing))
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🎉", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(8.dp))
                Text("Quest réussi !", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ta photo est validée. Le mur du jour est ouvert — " +
                        "va voir qui d'autre y est allé.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔥", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ton streak continue !",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        // Particules jaillissant du centre de la carte
        val fraction = progress.value
        particles.forEach { particle ->
            val x = (cos(particle.angleRad) * particle.distance * fraction).toFloat()
            val y = (sin(particle.angleRad) * particle.distance * fraction).toFloat()
            Text(
                particle.emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = x * density
                        translationY = y * density
                        rotationZ = particle.rotations * 360f * fraction
                        scaleX = particle.scale
                        scaleY = particle.scale
                    }
                    .alpha((1f - fraction).coerceIn(0f, 1f)),
            )
        }
    }
}
