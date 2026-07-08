package com.quest.app.core

import android.content.Context
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Source de vérité locale (MVP sans backend).
 * Données de démo en attendant l'intégration API.
 */
class QuestRepository private constructor(private val appContext: Context) {

    companion object {
        @Volatile
        private var instance: QuestRepository? = null

        fun get(context: Context): QuestRepository =
            instance ?: synchronized(this) {
                instance ?: QuestRepository(context.applicationContext).also { instance = it }
            }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val me = User(
        id = "u-me",
        username = "toi",
        cityId = "montreal",
        streak = 4,
        totalCheckIns = 12,
    )

    private val todayQuest = Quest(
        id = "q-${LocalDate.now()}",
        date = LocalDate.now().toString(),
        cityId = "montreal",
        poi = Poi(
            id = "poi-tam-tams",
            name = "Les Tam-Tams du Mont-Royal",
            description = "Chaque dimanche depuis les années 80, des percussionnistes " +
                "se rassemblent spontanément au pied du monument George-Étienne Cartier. " +
                "Personne ne les a jamais organisés.",
            latitude = 45.5145,
            longitude = -73.5872,
            rarity = Rarity.RARE,
        ),
        notifiedAtEpochMs = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
        expiresAtEpochMs = System.currentTimeMillis() + 6 * 60 * 60 * 1000,
    )

    private val myCheckInFlow = MutableStateFlow<CheckIn?>(null)

    private val wallPhotos = listOf(
        WallPhoto("c-1", "alex", System.currentTimeMillis() - 3_600_000, reactions = 8),
        WallPhoto("c-2", "juju", System.currentTimeMillis() - 1_800_000, reactions = 3),
        WallPhoto("c-3", "marion", System.currentTimeMillis() - 600_000, reactions = 1),
    )

    private val leaderboard = (listOf(me) + listOf(
        User("u-1", "alex", "montreal", streak = 21, totalCheckIns = 87),
        User("u-2", "juju", "montreal", streak = 9, totalCheckIns = 34),
        User("u-3", "marion", "montreal", streak = 2, totalCheckIns = 15),
    )).sortedByDescending { it.streak }
        .mapIndexed { i, user -> LeaderboardEntry(user, i + 1, checkedToday = i % 2 == 0) }

    private val history = (1..6).map { daysAgo ->
        val date = LocalDate.now().minusDays(daysAgo.toLong())
        val quest = todayQuest.copy(
            id = "q-$date",
            date = date.toString(),
            poi = todayQuest.poi.copy(
                name = "Lieu du $date",
                rarity = if (daysAgo % 5 == 0) Rarity.LEGENDARY else Rarity.COMMON,
            ),
        )
        val checkIn = if (daysAgo % 3 == 0) null else CheckIn(
            id = "c-$date",
            questId = quest.id,
            userId = me.id,
            photoUrl = "",
            submittedAtEpochMs = 0,
            status = CheckInStatus.VALIDATED,
            validatedBy = "u-1",
        )
        HistoryEntry(quest, checkIn)
    }

    val uiState = combine(myCheckInFlow) { checkIns ->
        val myCheckIn = checkIns[0]
        QuestUiState(
            todayQuest = todayQuest,
            myCheckIn = myCheckIn,
            wallPhotos = wallPhotos,
            leaderboard = leaderboard,
            history = history,
        )
    }.stateIn(scope, SharingStarted.Eagerly, QuestUiState(todayQuest = todayQuest))

    fun submitCheckIn() {
        scope.launch {
            delay(300)
            myCheckInFlow.value = CheckIn(
                id = UUID.randomUUID().toString(),
                questId = todayQuest.id,
                userId = me.id,
                photoUrl = "",
                submittedAtEpochMs = System.currentTimeMillis(),
                status = CheckInStatus.PENDING,
            )
        }
    }
}
