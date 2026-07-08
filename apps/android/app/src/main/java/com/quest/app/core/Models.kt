package com.quest.app.core

/** Rareté d'un lieu : Commun (quotidien), Rare (hebdo), Légendaire (mensuel). */
enum class Rarity {
    COMMON,
    RARE,
    LEGENDARY,
}

data class Poi(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val rarity: Rarity,
)

data class Quest(
    val id: String,
    val date: String,
    val cityId: String,
    val poi: Poi,
    val notifiedAtEpochMs: Long,
    val expiresAtEpochMs: Long,
)

enum class CheckInStatus {
    PENDING,
    VALIDATED,
    REJECTED,
}

data class CheckIn(
    val id: String,
    val questId: String,
    val userId: String,
    val photoUrl: String,
    val submittedAtEpochMs: Long,
    val status: CheckInStatus,
    val validatedBy: String? = null,
)

data class User(
    val id: String,
    val username: String,
    val cityId: String,
    val streak: Int = 0,
    val totalCheckIns: Int = 0,
)

data class LeaderboardEntry(
    val user: User,
    val rank: Int,
    val checkedToday: Boolean,
)

data class WallPhoto(
    val checkInId: String,
    val username: String,
    val takenAtEpochMs: Long,
    val reactions: Int = 0,
)

data class HistoryEntry(
    val quest: Quest,
    val checkIn: CheckIn?,
)

/** État agrégé consommé par les panneaux Compose. */
data class QuestUiState(
    val todayQuest: Quest? = null,
    val myCheckIn: CheckIn? = null,
    val wallPhotos: List<WallPhoto> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
) {
    val wallUnlocked: Boolean get() = myCheckIn != null
}
