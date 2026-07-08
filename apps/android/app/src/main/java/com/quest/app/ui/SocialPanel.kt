package com.quest.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.quest.app.ui.theme.Sun

@Composable
fun SocialPanel(state: QuestUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Classement", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Les membres de ta ville, classés par jours d'affilée avec un quest réussi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.leaderboard, key = { it.user.id }) { entry ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = if (entry.rank == 1) Mint else MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Avatar cercle dégradé
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    if (entry.rank == 1) listOf(Sun, Lime)
                                    else listOf(Mint, Lime.copy(alpha = 0.4f)),
                                ),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (entry.rank == 1) "🌻" else entry.user.username.first().uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "@${entry.user.username}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (entry.checkedToday) {
                            Text(
                                "✓ a checké aujourd'hui",
                                style = MaterialTheme.typography.labelMedium,
                                color = Leaf,
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${entry.user.streak}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Leaf,
                        )
                        Text(
                            "jours 🔥",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
