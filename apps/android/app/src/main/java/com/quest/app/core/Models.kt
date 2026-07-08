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
    /** Photo Wikipedia du lieu, si disponible. */
    val photoUrl: String? = null,
)

data class Quest(
    val id: String,
    val date: String,
    /** Nom de la ville détectée (ex. « Montréal »). */
    val cityName: String,
    val poi: Poi,
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
    val photoUrl: String,
    val takenAtEpochMs: Long,
    val reactions: Int = 0,
)

data class HistoryEntry(
    val quest: Quest,
    val checkIn: CheckIn?,
)

/** Étapes de chargement du quest du jour. */
enum class LoadState {
    /** Il faut d'abord la permission de localisation. */
    NEEDS_PERMISSION,
    /** Recherche de la ville et du lieu du jour. */
    LOADING,
    /** Réseau ou géoloc indisponible — bouton réessayer. */
    ERROR,
    /** Quest prêt. */
    READY,
}

/** État agrégé consommé par les panneaux Compose. */
data class QuestUiState(
    val loadState: LoadState = LoadState.LOADING,
    val todayQuest: Quest? = null,
    /** Distance entre moi et le lieu, en mètres (si position connue). */
    val distanceMeters: Float? = null,
    val myLatitude: Double? = null,
    val myLongitude: Double? = null,
    val myCheckIn: CheckIn? = null,
    val streak: Int = 0,
    val wallPhotos: List<WallPhoto> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
) {
    val wallUnlocked: Boolean get() = myCheckIn != null
}
