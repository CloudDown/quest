package com.quest.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.quest.app.core.CheckInStatus
import com.quest.app.core.LoadState
import com.quest.app.core.QuestUiState
import com.quest.app.core.Rarity
import com.quest.app.ui.theme.RarityCommon
import com.quest.app.ui.theme.RarityLegendary
import com.quest.app.ui.theme.RarityRare

@Composable
fun TodayPanel(
    state: QuestUiState,
    onCheckInClick: () -> Unit,
    onMapClick: () -> Unit,
    onGrantPermission: () -> Unit,
    onRetry: () -> Unit,
) {
    when (state.loadState) {
        LoadState.NEEDS_PERMISSION -> CenterState(
            emoji = "📍",
            title = "Active ta position",
            body = "Pour trouver le lieu du jour dans ta ville.",
            action = "Continuer" to onGrantPermission,
        )
        LoadState.LOADING -> CenterState(
            emoji = null,
            title = "Chargement…",
            body = null,
            loading = true,
        )
        LoadState.ERROR -> CenterState(
            emoji = "🍂",
            title = "Pas de lieu",
            body = "Vérifie le réseau et la localisation.",
            action = "Réessayer" to onRetry,
        )
        LoadState.READY -> QuestContent(state, onCheckInClick, onMapClick)
    }
}

@Composable
private fun CenterState(
    emoji: String?,
    title: String,
    body: String?,
    action: Pair<String, () -> Unit>? = null,
    loading: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            emoji != null -> Text(emoji, style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
        if (body != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (action != null) {
            Spacer(Modifier.height(24.dp))
            PillButton(action.first, action.second)
        }
    }
}

@Composable
private fun PillButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun QuestContent(
    state: QuestUiState,
    onCheckInClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    val quest = state.todayQuest ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Lieu du jour",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(MaterialTheme.shapes.extraLarge),
        ) {
            if (quest.poi.photoUrl != null) {
                AsyncImage(
                    model = quest.poi.photoUrl,
                    contentDescription = quest.poi.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🌳", style = MaterialTheme.typography.displayLarge)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
            )
            Box(Modifier.padding(14.dp).align(Alignment.TopStart)) {
                RarityBadge(quest.poi.rarity)
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "Aujourd’hui à ${quest.cityName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Text(
                    quest.poi.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }
        }

        // Chips distance + date
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.distanceMeters?.let { meters ->
                InfoChip("🚶", formatDistance(meters))
            }
            InfoChip("📅", quest.date)
        }

        when (state.myCheckIn?.status) {
            null -> PillButton("Je suis là", onCheckInClick)
            CheckInStatus.PENDING -> Text(
                "En attente de validation…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            CheckInStatus.VALIDATED -> ValidatedCelebration()
            CheckInStatus.REJECTED -> Text(
                "Non validé — demain est un nouveau jour.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Extrait Wikipedia
        val blurb = quest.poi.description.trim()
        if (blurb.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    if (blurb.length > 280) blurb.take(277) + "…" else blurb,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        // Carte OSM intégrée (tap → plein écran)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMapClick,
                ),
        ) {
            QuestMap(
                poi = quest.poi,
                myLatitude = state.myLatitude,
                myLongitude = state.myLongitude,
                modifier = Modifier.fillMaxSize(),
                interactive = false,
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
            ) {
                Text(
                    "Agrandir",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoChip(emoji: String, label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            "$emoji $label",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

private fun formatDistance(meters: Float): String = when {
    meters < 1_000 -> "${meters.toInt()} m"
    else -> "%.1f km".format(meters / 1_000)
}

@Composable
fun RarityBadge(rarity: Rarity) {
    val (label, bg, fg) = when (rarity) {
        Rarity.COMMON -> Triple("Commun", MaterialTheme.colorScheme.surface, RarityCommon)
        Rarity.RARE -> Triple("Rare", MaterialTheme.colorScheme.surface, RarityRare)
        Rarity.LEGENDARY -> Triple("Légendaire", RarityLegendary, Color.White)
    }
    Surface(shape = MaterialTheme.shapes.small, color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (rarity != Rarity.LEGENDARY) {
                Box(Modifier.size(7.dp).background(fg, CircleShape))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
        }
    }
}
