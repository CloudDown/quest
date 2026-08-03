package com.quest.app.core

import android.content.Context
import android.location.Location
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.WeekFields
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "quest")

/**
 * Source de vérité du quest du jour + social.
 *
 * Auth guest automatique → API `server/` pour lieu partagé, check-in, mur, amis.
 * Fallback Wikipedia local si le serveur est injoignable (social alors vide).
 */
class QuestRepository private constructor(private val appContext: Context) {

    companion object {
        private val KEY_QUEST = stringPreferencesKey("today_quest_json")
        private val KEY_CHECKIN = stringPreferencesKey("today_checkin_json")
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("auth_user_id")
        private val KEY_USERNAME = stringPreferencesKey("auth_username")
        private val KEY_PASSWORD = stringPreferencesKey("auth_password")
        private val KEY_EMAIL = stringPreferencesKey("auth_email")

        @Volatile
        private var instance: QuestRepository? = null

        fun get(context: Context): QuestRepository =
            instance ?: synchronized(this) {
                instance ?: QuestRepository(context.applicationContext).also { instance = it }
            }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val locationService = LocationService(appContext)

    private val loadStateFlow = MutableStateFlow(LoadState.LOADING)
    private val questFlow = MutableStateFlow<Quest?>(null)
    private val positionFlow = MutableStateFlow<Pair<Double, Double>?>(null)
    private val distanceFlow = MutableStateFlow<Float?>(null)
    private val myCheckInFlow = MutableStateFlow<CheckIn?>(null)
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val streakFlow = MutableStateFlow(0)
    private val wallFlow = MutableStateFlow<List<WallPhoto>>(emptyList())
    private val leaderboardFlow = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    private val historyFlow = MutableStateFlow<List<HistoryEntry>>(emptyList())
    private val myUserIdFlow = MutableStateFlow("")
    private val usernameFlow = MutableStateFlow("explorateur")
    private val onlineFlow = MutableStateFlow(false)

    private val uiStateFlow = MutableStateFlow(QuestUiState())

    val uiState = uiStateFlow.stateIn(scope, SharingStarted.Eagerly, QuestUiState())

    private fun publish() {
        val pos = positionFlow.value
        uiStateFlow.value = QuestUiState(
            loadState = loadStateFlow.value,
            todayQuest = questFlow.value,
            distanceMeters = distanceFlow.value,
            myLatitude = pos?.first,
            myLongitude = pos?.second,
            myCheckIn = myCheckInFlow.value,
            streak = streakFlow.value,
            themeMode = themeModeFlow.value,
            hasLocationPermission = locationService.hasPermission(),
            wallPhotos = wallFlow.value,
            leaderboard = leaderboardFlow.value,
            history = historyFlow.value,
            myUserId = myUserIdFlow.value,
            username = usernameFlow.value,
            serverOnline = onlineFlow.value,
        )
    }

    init {
        scope.launch {
            val stored = appContext.dataStore.data.first()[KEY_THEME]
            themeModeFlow.value = stored?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM
            publish()
        }
        refresh()
    }

    fun setThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
        publish()
        scope.launch {
            appContext.dataStore.edit { it[KEY_THEME] = mode.name }
        }
    }

    fun refresh() {
        scope.launch {
            if (!locationService.hasPermission()) {
                loadStateFlow.value = LoadState.NEEDS_PERMISSION
                publish()
                return@launch
            }
            loadStateFlow.value = LoadState.LOADING
            clearSocial()
            publish()

            ensureAuth()
            publish()

            val today = LocalDate.now().toString()
            val prefs = appContext.dataStore.data.first()
            prefs[KEY_CHECKIN]?.let { checkInFromJson(it) }
                ?.takeIf { it.questId.contains(today) || it.questId.toIntOrNull() != null }
                ?.let { myCheckInFlow.value = it }

            val location = locationService.currentLocation()
            if (location == null) {
                loadStateFlow.value = LoadState.ERROR
                publish()
                return@launch
            }
            val city = locationService.cityAt(location.latitude, location.longitude)
            if (city == null) {
                loadStateFlow.value = LoadState.ERROR
                publish()
                return@launch
            }
            positionFlow.value = location.latitude to location.longitude

            val online = QuestApi.healthOk() && QuestApi.accessToken != null
            onlineFlow.value = online

            val quest = if (online) {
                try {
                    val api = QuestApi.todayQuest(
                        cityName = city.name,
                        centerLat = city.centerLatitude,
                        centerLon = city.centerLongitude,
                        userLat = location.latitude,
                        userLon = location.longitude,
                    )
                    api.toQuest().also { q ->
                        api.distanceMeters?.let { distanceFlow.value = it }
                            ?: updateDistance(q)
                    }
                } catch (_: Exception) {
                    onlineFlow.value = false
                    localQuest(city, today)
                }
            } else {
                localQuest(city, today)
            }

            if (quest == null) {
                loadStateFlow.value = LoadState.ERROR
                publish()
                return@launch
            }

            questFlow.value = quest
            appContext.dataStore.edit { it[KEY_QUEST] = questToJson(quest) }
            if (distanceFlow.value == null) updateDistance(quest)

            syncFromServer(quest)
            loadStateFlow.value = LoadState.READY
            publish()
        }
    }

    fun submitCheckIn() {
        val quest = questFlow.value ?: return
        scope.launch {
            val questId = quest.id.toIntOrNull()
            if (onlineFlow.value && questId != null && QuestApi.accessToken != null) {
                try {
                    val api = QuestApi.submitCheckIn(questId, quest.poi.photoUrl)
                    val checkIn = api.toCheckIn()
                    myCheckInFlow.value = checkIn
                    appContext.dataStore.edit { it[KEY_CHECKIN] = checkInToJson(checkIn) }
                    syncFromServer(quest)
                    publish()
                    return@launch
                } catch (_: Exception) {
                    // fallback local ci-dessous
                }
            }

            // Hors ligne : check-in local uniquement (pas de faux amis / mur démo)
            val checkIn = CheckIn(
                id = "local-${UUID.randomUUID()}",
                questId = quest.id,
                userId = myUserIdFlow.value.ifEmpty { "local" },
                photoUrl = quest.poi.photoUrl.orEmpty(),
                submittedAtEpochMs = System.currentTimeMillis(),
                status = CheckInStatus.VALIDATED,
            )
            myCheckInFlow.value = checkIn
            streakFlow.value = (streakFlow.value + 1).coerceAtLeast(1)
            appContext.dataStore.edit { it[KEY_CHECKIN] = checkInToJson(checkIn) }
            wallFlow.value = listOf(
                WallPhoto(
                    checkInId = checkIn.id,
                    username = usernameFlow.value,
                    photoUrl = checkIn.photoUrl,
                    takenAtEpochMs = checkIn.submittedAtEpochMs,
                ),
            )
            historyFlow.value = listOf(HistoryEntry(quest, checkIn)) + historyFlow.value
            leaderboardFlow.value = listOf(
                LeaderboardEntry(
                    user = User(
                        id = checkIn.userId,
                        username = usernameFlow.value,
                        cityId = quest.cityName,
                        streak = streakFlow.value,
                        totalCheckIns = 1,
                    ),
                    rank = 1,
                    checkedToday = true,
                ),
            )
            publish()
        }
    }

    private suspend fun ensureAuth() {
        val prefs = appContext.dataStore.data.first()
        val token = prefs[KEY_TOKEN]
        val email = prefs[KEY_EMAIL]
        val password = prefs[KEY_PASSWORD]
        val userId = prefs[KEY_USER_ID]
        val username = prefs[KEY_USERNAME]

        if (token != null && userId != null && username != null) {
            QuestApi.accessToken = token
            myUserIdFlow.value = userId
            usernameFlow.value = username
            try {
                val me = QuestApi.me()
                streakFlow.value = me.streak
                usernameFlow.value = me.username
                myUserIdFlow.value = me.id.toString()
                return
            } catch (_: Exception) {
                // token périmé → re-login / register
            }
        }

        val mail = email ?: "device_${UUID.randomUUID().toString().take(8)}@quest.local"
        val pass = password ?: UUID.randomUUID().toString()
        val name = username ?: "explorer_${UUID.randomUUID().toString().take(6)}"

        try {
            val result = when {
                email != null && password != null -> {
                    try {
                        QuestApi.login(mail, pass)
                    } catch (_: Exception) {
                        registerGuest(mail, name, pass)
                    }
                }
                else -> registerGuest(mail, name, pass)
            }
            QuestApi.accessToken = result.accessToken
            myUserIdFlow.value = result.userId.toString()
            usernameFlow.value = result.username
            appContext.dataStore.edit {
                it[KEY_TOKEN] = result.accessToken
                it[KEY_USER_ID] = result.userId.toString()
                it[KEY_USERNAME] = result.username
                it[KEY_EMAIL] = mail
                it[KEY_PASSWORD] = pass
            }
            runCatching { QuestApi.me() }.getOrNull()?.let { streakFlow.value = it.streak }
        } catch (_: Exception) {
            QuestApi.accessToken = null
            if (myUserIdFlow.value.isEmpty()) {
                myUserIdFlow.value = "local"
                usernameFlow.value = name
            }
        }
    }

    private suspend fun registerGuest(email: String, username: String, password: String): ApiToken {
        return try {
            QuestApi.register(email, username, password)
        } catch (_: Exception) {
            val alt = "explorer_${UUID.randomUUID().toString().take(6)}"
            val mail = "device_${UUID.randomUUID().toString().take(8)}@quest.local"
            QuestApi.register(mail, alt, password)
        }
    }

    private suspend fun syncFromServer(quest: Quest) {
        if (!onlineFlow.value || QuestApi.accessToken == null) return
        val questId = quest.id.toIntOrNull() ?: return
        try {
            QuestApi.myCheckIn()?.let { api ->
                val checkIn = api.toCheckIn()
                myCheckInFlow.value = checkIn
                appContext.dataStore.edit { it[KEY_CHECKIN] = checkInToJson(checkIn) }
            }
            wallFlow.value = QuestApi.wall(questId).map { it.toWallPhoto() }
            leaderboardFlow.value = QuestApi.leaderboard(quest.cityName).map { it.toEntry() }
            historyFlow.value = QuestApi.history().map { it.toHistory() }
            QuestApi.me().let {
                streakFlow.value = it.streak
                usernameFlow.value = it.username
                myUserIdFlow.value = it.id.toString()
            }
        } catch (_: Exception) {
            // garde l'état local
        }
    }

    private fun clearSocial() {
        wallFlow.value = emptyList()
        leaderboardFlow.value = emptyList()
        // history / streak conservés jusqu'à sync
    }

    private suspend fun localQuest(city: City, today: String): Quest? {
        val prefs = appContext.dataStore.data.first()
        val cached = prefs[KEY_QUEST]?.let { questFromJson(it) }
        if (cached != null && cached.date == today) {
            updateDistance(cached)
            return cached
        }
        val places = try {
            WikipediaApi.placesAround(city.centerLatitude, city.centerLongitude)
        } catch (_: Exception) {
            emptyList()
        }
        if (places.isEmpty()) return null
        return pickDailyQuest(city, places, LocalDate.now()).also { updateDistance(it) }
    }

    private fun pickDailyQuest(city: City, places: List<WikiPlace>, date: LocalDate): Quest {
        val rarity = rarityFor(city.name, date)
        val sorted = places.sortedBy { it.pageId }
        val candidates = if (rarity != Rarity.COMMON) {
            sorted.filter { it.photoUrl != null }.ifEmpty { sorted }
        } else {
            sorted
        }
        val seed = "${city.name.lowercase()}|$date".hashCode().toLong()
        val place = candidates[Random(seed).nextInt(candidates.size)]
        return Quest(
            id = "q-${city.name.lowercase()}-$date",
            date = date.toString(),
            cityName = city.name,
            poi = Poi(
                id = place.pageId.toString(),
                name = place.title,
                description = place.description,
                latitude = place.latitude,
                longitude = place.longitude,
                rarity = rarity,
                photoUrl = place.photoUrl,
            ),
        )
    }

    private fun rarityFor(cityName: String, date: LocalDate): Rarity {
        val city = cityName.lowercase()
        val legendaryDay = Random("$city|${date.year}-${date.monthValue}".hashCode().toLong())
            .nextInt(1, date.lengthOfMonth() + 1)
        if (date.dayOfMonth == legendaryDay) return Rarity.LEGENDARY
        val week = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        val rareDay = Random("$city|${date.year}-w$week".hashCode().toLong()).nextInt(1, 8)
        if (date.dayOfWeek.value == rareDay) return Rarity.RARE
        return Rarity.COMMON
    }

    private suspend fun updateDistance(quest: Quest) {
        val location = locationService.currentLocation() ?: return
        positionFlow.value = location.latitude to location.longitude
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            quest.poi.latitude,
            quest.poi.longitude,
            results,
        )
        distanceFlow.value = results[0]
    }

    private fun ApiQuest.toQuest(): Quest {
        val rarity = runCatching { Rarity.valueOf(rarity) }.getOrDefault(Rarity.COMMON)
        return Quest(
            id = id.toString(),
            date = date,
            cityName = cityName,
            poi = Poi(
                id = id.toString(),
                name = poiName,
                description = poiDescription,
                latitude = latitude,
                longitude = longitude,
                rarity = rarity,
                photoUrl = photoUrl,
            ),
        )
    }

    private fun ApiCheckIn.toCheckIn(): CheckIn {
        val status = runCatching { CheckInStatus.valueOf(status) }.getOrDefault(CheckInStatus.PENDING)
        val epoch = runCatching {
            OffsetDateTime.parse(submittedAt).toInstant().toEpochMilli()
        }.getOrDefault(System.currentTimeMillis())
        return CheckIn(
            id = id.toString(),
            questId = questId.toString(),
            userId = userId.toString(),
            photoUrl = "",
            submittedAtEpochMs = epoch,
            status = status,
            validatedBy = validatedBy?.toString(),
        )
    }

    private fun ApiWallPhoto.toWallPhoto() = WallPhoto(
        checkInId = checkInId.toString(),
        username = username,
        photoUrl = photoUrl.orEmpty(),
        takenAtEpochMs = System.currentTimeMillis(),
        reactions = reactions,
    )

    private fun ApiLeaderboardEntry.toEntry() = LeaderboardEntry(
        user = User(
            id = userId.toString(),
            username = username,
            cityId = "",
            streak = streak,
            totalCheckIns = totalCheckIns,
        ),
        rank = rank,
        checkedToday = checkedToday,
    )

    private fun ApiHistoryItem.toHistory(): HistoryEntry {
        val q = quest.toQuest()
        return HistoryEntry(q, checkIn.toCheckIn())
    }

    private fun questToJson(quest: Quest): String = JSONObject().apply {
        put("id", quest.id)
        put("date", quest.date)
        put("cityName", quest.cityName)
        put("poiId", quest.poi.id)
        put("name", quest.poi.name)
        put("description", quest.poi.description)
        put("lat", quest.poi.latitude)
        put("lon", quest.poi.longitude)
        put("rarity", quest.poi.rarity.name)
        put("photoUrl", quest.poi.photoUrl ?: "")
    }.toString()

    private fun questFromJson(json: String): Quest? = try {
        val obj = JSONObject(json)
        Quest(
            id = obj.getString("id"),
            date = obj.getString("date"),
            cityName = obj.getString("cityName"),
            poi = Poi(
                id = obj.getString("poiId"),
                name = obj.getString("name"),
                description = obj.getString("description"),
                latitude = obj.getDouble("lat"),
                longitude = obj.getDouble("lon"),
                rarity = Rarity.valueOf(obj.getString("rarity")),
                photoUrl = obj.getString("photoUrl").ifEmpty { null },
            ),
        )
    } catch (_: Exception) {
        null
    }

    private fun checkInToJson(checkIn: CheckIn): String = JSONObject().apply {
        put("id", checkIn.id)
        put("questId", checkIn.questId)
        put("userId", checkIn.userId)
        put("status", checkIn.status.name)
        put("submittedAt", checkIn.submittedAtEpochMs)
        put("validatedBy", checkIn.validatedBy ?: "")
        put("photoUrl", checkIn.photoUrl)
    }.toString()

    private fun checkInFromJson(json: String): CheckIn? = try {
        val obj = JSONObject(json)
        CheckIn(
            id = obj.getString("id"),
            questId = obj.getString("questId"),
            userId = obj.optString("userId", "local"),
            photoUrl = obj.optString("photoUrl"),
            submittedAtEpochMs = obj.getLong("submittedAt"),
            status = CheckInStatus.valueOf(obj.getString("status")),
            validatedBy = obj.optString("validatedBy").ifEmpty { null },
        )
    } catch (_: Exception) {
        null
    }
}
