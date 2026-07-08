package com.quest.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quest.app.core.CheckInStatus
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
) {
    val quest = state.todayQuest
    if (quest == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🌱", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                "Pas encore de quest aujourd'hui",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La notif peut tomber à tout moment. Reste prêt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // En-tête : date + rareté
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Quest du jour",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(quest.date, style = MaterialTheme.typography.titleLarge)
            }
            RarityBadge(quest.poi.rarity)
        }

        // Carte héro : le lieu, posé sur un dégradé végétal
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = Mint,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(listOf(Lime, Sun)),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📍", style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    quest.poi.name,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    quest.poi.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Carte (placeholder) — texture douce vert brume
        Surface(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🗺️", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${quest.poi.latitude}, ${quest.poi.longitude}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Action / statut du check-in
        when (state.myCheckIn?.status) {
            null -> Button(
                onClick = onCheckInClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("J'y suis — check-in photo", style = MaterialTheme.typography.titleMedium)
            }

            CheckInStatus.PENDING -> StatusCard(
                emoji = "⏳",
                text = "Check-in soumis. En attente de validation par un pair…",
                tint = Sun.copy(alpha = 0.25f),
            )

            CheckInStatus.VALIDATED -> StatusCard(
                emoji = "🌿",
                text = "Check-in validé. Le mur est déverrouillé !",
                tint = Lime.copy(alpha = 0.3f),
            )

            CheckInStatus.REJECTED -> StatusCard(
                emoji = "🍂",
                text = "Pas de validation à temps. Retente demain.",
                tint = Honey.copy(alpha = 0.2f),
            )
        }

        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun StatusCard(emoji: String, text: String, tint: androidx.compose.ui.graphics.Color) {
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
        Rarity.LEGENDARY -> "Légendaire" to RarityLegendary
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (rarity == Rarity.LEGENDARY) Honey else Leaf,
            )
        }
    }
}
