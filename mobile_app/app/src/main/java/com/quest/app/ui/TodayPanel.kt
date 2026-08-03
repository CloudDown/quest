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
import com.quest.app.ui.theme.AmberSoft
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
        LoadState.NEEDS_PERMISSION -> EmptyState(
            emoji = "📍",
            title = "Où es-tu ?",
            body = "Quest trouve ta ville pour révéler le lieu du jour — le même pour tous.",
            actionLabel = "Autoriser la localisation",
            onAction = onGrantPermission,
        )
        LoadState.LOADING -> EmptyState(
            emoji = null,
            title = "Recherche du lieu…",
            body = "Un instant.",
            loading = true,
        )
        LoadState.ERROR -> EmptyState(
            emoji = "🍂",
            title = "Impossible de charger",
            body = "Vérifie le réseau et la localisation.",
            actionLabel = "Réessayer",
            onAction = onRetry,
        )
        LoadState.READY -> QuestContent(state, onCheckInClick, onMapClick)
    }
}

@Composable
private fun QuestContent(
    state: QuestUiState,
    onCheckInClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    val quest = state.todayQuest ?: return

    FadeInColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Héro photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
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
                                0f to Color.Black.copy(alpha = 0.08f),
                                0.45f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.78f),
                            ),
                        ),
                )
                Box(Modifier.padding(14.dp).align(Alignment.TopStart)) {
                    RarityBadge(quest.poi.rarity)
                }
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        quest.cityName.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Text(
                        quest.poi.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.distanceMeters?.let { InfoChip("🚶", formatDistance(it)) }
                if (quest.poi.rarity == Rarity.LEGENDARY) {
                    InfoChip("✨", "Jour légendaire")
                } else if (quest.poi.rarity == Rarity.RARE) {
                    InfoChip("🌿", "Jour rare")
                }
            }

            when (state.myCheckIn?.status) {
                null -> QuestPrimaryButton("Je suis sur place", onCheckInClick)
                CheckInStatus.PENDING -> StatusBanner(
                    emoji = "⏳",
                    title = "Photo envoyée",
                    body = "Un autre Quester doit la valider.",
                    tint = AmberSoft,
                )
                CheckInStatus.VALIDATED -> ValidatedCelebration()
                CheckInStatus.REJECTED -> StatusBanner(
                    emoji = "🍂",
                    title = "Non validée",
                    body = "Demain, un nouveau lieu t’attend.",
                )
            }

            val blurb = quest.poi.description.trim()
            if (blurb.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "Le lieu",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (blurb.length > 260) blurb.take(257) + "…" else blurb,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                ) {
                    Text(
                        "Voir la carte",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(emoji: String, label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            "$emoji  $label",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
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
    Surface(shape = CircleShape, color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
