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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.quest.app.ui.theme.Honey
import com.quest.app.ui.theme.Leaf
import com.quest.app.ui.theme.Lime
import com.quest.app.ui.theme.Mint
import com.quest.app.ui.theme.RarityCommon
import com.quest.app.ui.theme.RarityLegendary
import com.quest.app.ui.theme.RarityRare
import com.quest.app.ui.theme.Sun

@Composable
fun TodayPanel(
    state: QuestUiState,
    onCheckInClick: () -> Unit,
    onMapClick: () -> Unit,
    onGrantPermission: () -> Unit,
    onRetry: () -> Unit,
) {
    when (state.loadState) {
        LoadState.NEEDS_PERMISSION -> PermissionPanel(onGrantPermission)
        LoadState.LOADING -> LoadingPanel()
        LoadState.ERROR -> ErrorPanel(onRetry)
        LoadState.READY -> state.todayQuest?.let { quest ->
            QuestContent(state, onCheckInClick, onMapClick)
        }
    }
}

// ── États ────────────────────────────────────────────────────────

@Composable
private fun PermissionPanel(onGrantPermission: () -> Unit) {
    CenteredPanel {
        BigEmojiCircle("🧭")
        Spacer(Modifier.height(24.dp))
        Text("Où es-tu ?", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Text(
            "Quest a besoin de ta position pour trouver ta ville " +
                "et te révéler le lieu du jour, partagé par tous ses habitants.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        PillButton("Autoriser la localisation", onGrantPermission)
    }
}

@Composable
private fun LoadingPanel() {
    CenteredPanel {
        CircularProgressIndicator(color = Lime)
        Spacer(Modifier.height(20.dp))
        Text("Recherche du lieu du jour…", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "On identifie ta ville et son lieu mystère.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorPanel(onRetry: () -> Unit) {
    CenteredPanel {
        BigEmojiCircle("🍂")
        Spacer(Modifier.height(24.dp))
        Text("Impossible de trouver le lieu", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))
        Text(
            "Vérifie ta connexion internet et que la localisation est activée, puis réessaie.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        PillButton("Réessayer", onRetry)
    }
}

@Composable
private fun CenteredPanel(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

@Composable
private fun BigEmojiCircle(emoji: String) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .background(Mint, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
private fun PillButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Lime,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

// ── Contenu principal ────────────────────────────────────────────

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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Héro : photo Wikipedia du lieu, badge rareté et nom par-dessus
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                        .background(Brush.linearGradient(listOf(Mint, Lime.copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🌳", style = MaterialTheme.typography.displayLarge)
                }
            }

            // Dégradé de lisibilité en bas de la photo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.65f),
                        ),
                    ),
            )

            Box(modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                RarityBadge(quest.poi.rarity)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
            ) {
                Text(
                    "Le lieu du jour à ${quest.cityName}",
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

        // Chips : distance + date
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.distanceMeters?.let { meters ->
                InfoChip("🚶", formatDistance(meters))
            }
            InfoChip("📅", quest.date)
        }

        // Description Wikipedia
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("L'histoire du lieu", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    quest.poi.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Carte intégrée (tap = plein écran)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
            ) {
                Text(
                    "Agrandir 🔎",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        // Action / statut
        when (state.myCheckIn?.status) {
            null -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = Mint,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Comment ça marche", style = MaterialTheme.typography.titleMedium)
                        StepRow("1", "Rends-toi sur place")
                        StepRow("2", "Prends une photo une fois arrivé")
                        StepRow("3", "Un autre membre valide ta photo")
                    }
                }
                PillButton("Je suis sur place — prendre la photo", onCheckInClick)
            }

            CheckInStatus.PENDING -> StatusCard(
                emoji = "⏳",
                text = "Photo envoyée ! Un autre membre doit la valider. " +
                    "Tu seras prévenu dès que c'est fait.",
                tint = Sun.copy(alpha = 0.25f),
            )

            CheckInStatus.VALIDATED -> ValidatedCelebration()

            CheckInStatus.REJECTED -> StatusCard(
                emoji = "🍂",
                text = "Personne n'a validé ta photo à temps. " +
                    "Pas grave — un nouveau lieu arrive demain.",
                tint = Honey.copy(alpha = 0.2f),
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoChip(emoji: String, label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            "$emoji $label",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private fun formatDistance(meters: Float): String = when {
    meters < 1_000 -> "à ${meters.toInt()} m"
    else -> "à %.1f km".format(meters / 1_000)
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Lime, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                number,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StatusCard(emoji: String, text: String, tint: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tint,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun RarityBadge(rarity: Rarity) {
    val (label, color) = when (rarity) {
        Rarity.COMMON -> "Commun" to RarityCommon
        Rarity.RARE -> "Rare" to RarityRare
        Rarity.LEGENDARY -> "Légendaire ✨" to RarityLegendary
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = if (rarity == Rarity.LEGENDARY) Honey else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (rarity != Rarity.LEGENDARY) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = when (rarity) {
                    Rarity.LEGENDARY -> Color.White
                    Rarity.RARE -> Leaf
                    Rarity.COMMON -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
