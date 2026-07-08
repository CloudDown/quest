package com.quest.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quest.app.core.QuestUiState
import com.quest.app.ui.theme.Leaf
import com.quest.app.ui.theme.Lime
import com.quest.app.ui.theme.Mint

@Composable
fun WallPanel(state: QuestUiState) {
    if (!state.wallUnlocked) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Mint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔒", style = MaterialTheme.typography.headlineLarge)
            }
            Spacer(Modifier.height(20.dp))
            Text("Le mur est encore verrouillé", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Ici s'affichent les photos de tous ceux qui sont allés au lieu du jour. " +
                    "Pour les voir, va d'abord sur place et fais ton check-in photo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text("Le mur du jour", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Les photos de tous ceux qui ont fait le quest aujourd'hui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.wallPhotos, key = { it.checkInId }) { photo ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    // Placeholder photo — dégradé végétal en attendant Coil
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .background(Brush.linearGradient(listOf(Mint, Lime.copy(alpha = 0.5f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🌄", style = MaterialTheme.typography.displaySmall)
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "@${photo.username}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Leaf,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "☀️ ${photo.reactions}",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}
