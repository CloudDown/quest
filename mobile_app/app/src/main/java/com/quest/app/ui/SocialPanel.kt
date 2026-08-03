package com.quest.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.quest.app.core.QuestUiState

@Composable
fun SocialPanel(state: QuestUiState) {
    val checkedCount = state.leaderboard.count { it.checkedToday }

    if (state.leaderboard.isEmpty()) {
        EmptyState(
            emoji = "👋",
            title = "Pas encore d’amis ici",
            body = if (state.serverOnline) {
                "Quand d’autres Questers de ${state.todayQuest?.cityName ?: "ta ville"} checkent, ils apparaissent ici."
            } else {
                "Connecte-toi au serveur pour voir les Questers de ta ville."
            },
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text("Amis", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (checkedCount == 0) "Personne n’a encore checké"
                    else "$checkedCount ont checké aujourd’hui",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.leaderboard, key = { it.user.id }) { entry ->
            val isMe = entry.user.id == state.myUserId
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = when {
                    isMe -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    entry.checkedToday -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (entry.checkedToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (isMe) "🌿" else entry.user.username.first().uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (entry.checkedToday && !isMe) {
                                MaterialTheme.colorScheme.onPrimary
                            } else if (entry.checkedToday) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isMe) "Toi" else entry.user.username,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            if (entry.checkedToday) "✓ Sur place aujourd’hui"
                            else "Pas encore",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (entry.checkedToday) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${entry.user.streak}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "jours",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
