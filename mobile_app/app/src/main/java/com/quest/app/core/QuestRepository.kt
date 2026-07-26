package com.quest.app.core

import android.content.Context
import android.location.Location
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "quest")

/**
 * Source de vérité du quest du jour.
 *
 * Le lieu est tiré des POIs Wikipedia autour du centre de la ville détectée,
 * avec un seed déterministe `ville + date` : tous les habitants d'une même
 * ville ont le même lieu le même jour. Le résultat est figé dans DataStore
 * pour la journée (stable et disponible hors ligne).
 *
 * Mur / classement / validations restent en démo (pas encore de backend).
 */
class QuestRepository private constructor(private val appContext: Context) {

    companion object {
        private val KEY_QUEST = stringPreferencesKey("today_quest_json")
        private val KEY_CHECKIN = stringPreferencesKey("today_checkin_json")
        private val KEY_THEME = stringPreferencesKey("theme_mode")

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

    private val coreState = combine(
        loadStateFlow,
        questFlow,
        positionFlow,
        distanceFlow,
        myCheckInFlow,
    ) { loadState, quest, position, distance, checkIn ->
        QuestUiState(
            loadState = loadState,
            todayQuest = quest,
            distanceMeters = distance,
            myLatitude = position?.first,
            myLongitude = position?.second,
            myCheckIn = checkIn,
            streak = DemoData.streak,
            hasLocationPermission = locationService.hasPermission(),
            wallPhotos = if (quest != null) DemoData.wallPhotos(quest) else emptyList(),
            leaderboard = DemoData.leaderboard,
            history = if (quest != null) DemoData.history(quest) else emptyList(),
        )
    }

    val uiState = combine(coreState, themeModeFlow) { state, themeMode ->
        state.copy(
            themeMode = themeMode,
            hasLocationPermission = locationService.hasPermission(),
        )
    }.stateIn(scope, SharingStarted.Eagerly, QuestUiState())

    init {
        scope.launch {
            val stored = appContext.dataStore.data.first()[KEY_THEME]
            themeModeFlow.value = stored?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM
        }
        refresh()
    }

    fun setThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
        scope.launch {
            appContext.dataStore.edit { it[KEY_THEME] = mode.name }
        }
    }

    /** (Re)charge le quest du jour. Appelé au démarrage, après permission, ou sur « réessayer ». */
    fun refresh() {
        scope.launch {
            if (!locationService.hasPermission()) {
                loadStateFlow.value = LoadState.NEEDS_PERMISSION
                return@launch
            }
            loadStateFlow.value = LoadState.LOADING

            // Restaure l'état du jour depuis le cache (quest + check-in)
            val today = LocalDate.now().toString()
            val prefs = appContext.dataStore.data.first()
            val cached = prefs[KEY_QUEST]?.let { questFromJson(it) }
            prefs[KEY_CHECKIN]?.let { checkInFromJson(it) }
                ?.takeIf { it.questId.endsWith(today) }
                ?.let { myCheckInFlow.value = it }

            if (cached != null && cached.date == today) {
                questFlow.value = cached
                updateDistance(cached)
                loadStateFlow.value = LoadState.READY
                return@launch
            }

            // Position → ville → POIs Wikipedia → tirage du jour
            val location = locationService.currentLocation()
            if (location == null) {
                loadStateFlow.value = LoadState.ERROR
                return@launch
            }
            val city = locationService.cityAt(location.latitude, location.longitude)
            if (city == null) {
                loadStateFlow.value = LoadState.ERROR
                return@launch
            }

            val places = try {
                WikipediaApi.placesAround(city.centerLatitude, city.centerLongitude)
            } catch (e: Exception) {
                emptyList()
            }
            if (places.isEmpty()) {
                loadStateFlow.value = LoadState.ERROR
                return@launch
            }

            val quest = pickDailyQuest(city, places, LocalDate.now())
            questFlow.value = quest
            updateDistance(quest)
            appContext.dataStore.edit { it[KEY_QUEST] = questToJson(quest) }
            loadStateFlow.value = LoadState.READY
        }
    }

    /**
     * Tirage déterministe : même ville + même date = même lieu pour tout le monde.
     * Les jours rares/légendaires privilégient les lieux avec photo (plus mémorables).
     */
    private fun pickDailyQuest(city: City, places: List<WikiPlace>, date: LocalDate): Quest {
        val rarity = rarityFor(city.name, date)
        val sorted = places.sortedBy { it.pageId } // ordre stable quel que soit l'appareil
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

    /**
     * Rythme produit : 1 jour Légendaire par mois, 1 jour Rare par semaine,
     * Commun sinon. Déterministe par ville.
     */
    private fun rarityFor(cityName: String, date: LocalDate): Rarity {
        val city = cityName.lowercase()

        // Jour légendaire du mois : choisi par hash(ville + année-mois)
        val legendaryDay = Random("$city|${date.year}-${date.monthValue}".hashCode().toLong())
            .nextInt(1, date.lengthOfMonth() + 1)
        if (date.dayOfMonth == legendaryDay) return Rarity.LEGENDARY

        // Jour rare de la semaine : choisi par hash(ville + année-semaine)
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

    fun submitCheckIn() {
        val quest = questFlow.value ?: return
        scope.launch {
            val checkIn = CheckIn(
                id = UUID.randomUUID().toString(),
                questId = quest.id,
                userId = "u-me",
                photoUrl = "",
                submittedAtEpochMs = System.currentTimeMillis(),
                status = CheckInStatus.PENDING,
            )
            myCheckInFlow.value = checkIn
            appContext.dataStore.edit { it[KEY_CHECKIN] = checkInToJson(checkIn) }

            // Démo : validation automatique après quelques secondes,
            // pour montrer la célébration (sera remplacé par le backend).
            kotlinx.coroutines.delay(5_000)
            val validated = checkIn.copy(status = CheckInStatus.VALIDATED, validatedBy = "u-1")
            myCheckInFlow.value = validated
            appContext.dataStore.edit { it[KEY_CHECKIN] = checkInToJson(validated) }
        }
    }

    // ── Sérialisation JSON (cache DataStore) ────────────────────────

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
    } catch (e: Exception) {
        null
    }

    private fun checkInToJson(checkIn: CheckIn): String = JSONObject().apply {
        put("id", checkIn.id)
        put("questId", checkIn.questId)
        put("status", checkIn.status.name)
        put("submittedAt", checkIn.submittedAtEpochMs)
        put("validatedBy", checkIn.validatedBy ?: "")
    }.toString()

    private fun checkInFromJson(json: String): CheckIn? = try {
        val obj = JSONObject(json)
        CheckIn(
            id = obj.getString("id"),
            questId = obj.getString("questId"),
            userId = "u-me",
            photoUrl = "",
            submittedAtEpochMs = obj.getLong("submittedAt"),
            status = CheckInStatus.valueOf(obj.getString("status")),
            validatedBy = obj.getString("validatedBy").ifEmpty { null },
        )
    } catch (e: Exception) {
        null
    }
}

/** Données sociales de démo, en attendant le backend. */
private object DemoData {
    const val streak = 4

    val leaderboard = listOf(
        User("u-1", "alex", "", streak = 21, totalCheckIns = 87),
        User("u-me", "toi", "", streak = 4, totalCheckIns = 12),
        User("u-2", "juju", "", streak = 9, totalCheckIns = 34),
        User("u-3", "marion", "", streak = 2, totalCheckIns = 15),
    ).sortedByDescending { it.streak }
        .mapIndexed { i, user -> LeaderboardEntry(user, i + 1, checkedToday = i % 2 == 0) }

    fun wallPhotos(quest: Quest) = listOf(
        WallPhoto("c-1", "alex", quest.poi.photoUrl ?: "", System.currentTimeMillis() - 3_600_000, 8),
        WallPhoto("c-2", "juju", quest.poi.photoUrl ?: "", System.currentTimeMillis() - 1_800_000, 3),
        WallPhoto("c-3", "marion", quest.poi.photoUrl ?: "", System.currentTimeMillis() - 600_000, 1),
    )

    fun history(quest: Quest) = (1..6).map { daysAgo ->
        val date = LocalDate.now().minusDays(daysAgo.toLong())
        val pastQuest = quest.copy(
            id = "q-$date",
            date = date.toString(),
            poi = quest.poi.copy(
                name = "Lieu du $date",
                rarity = if (daysAgo % 5 == 0) Rarity.LEGENDARY else Rarity.COMMON,
                photoUrl = null,
            ),
        )
        val checkIn = if (daysAgo % 3 == 0) null else CheckIn(
            id = "c-$date",
            questId = pastQuest.id,
            userId = "u-me",
            photoUrl = "",
            submittedAtEpochMs = 0,
            status = CheckInStatus.VALIDATED,
            validatedBy = "u-1",
        )
        HistoryEntry(pastQuest, checkIn)
    }
}
