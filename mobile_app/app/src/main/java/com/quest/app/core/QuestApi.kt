package com.quest.app.core

import com.quest.app.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ApiToken(
    val accessToken: String,
    val userId: Int,
    val username: String,
)

data class ApiUser(
    val id: Int,
    val username: String,
    val cityName: String?,
    val streak: Int,
    val totalCheckIns: Int,
)

data class ApiQuest(
    val id: Int,
    val date: String,
    val cityName: String,
    val poiName: String,
    val poiDescription: String,
    val latitude: Double,
    val longitude: Double,
    val rarity: String,
    val photoUrl: String?,
    val distanceMeters: Float?,
)

data class ApiCheckIn(
    val id: Int,
    val questId: Int,
    val userId: Int,
    val status: String,
    val validatedBy: Int?,
    val submittedAt: String,
)

data class ApiWallPhoto(
    val checkInId: Int,
    val username: String,
    val photoUrl: String?,
    val reactions: Int,
)

data class ApiLeaderboardEntry(
    val userId: Int,
    val username: String,
    val streak: Int,
    val totalCheckIns: Int,
    val rank: Int,
    val checkedToday: Boolean,
)

data class ApiHistoryItem(
    val quest: ApiQuest,
    val checkIn: ApiCheckIn,
)

/**
 * Client HTTP vers `server/` (auth, quests, social).
 * HttpURLConnection + JSON — même style que [WikipediaApi].
 */
object QuestApi {
    val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')

    @Volatile
    var accessToken: String? = null

    suspend fun register(email: String, username: String, password: String): ApiToken =
        postJson(
            "/auth/register",
            JSONObject()
                .put("email", email)
                .put("username", username)
                .put("password", password),
            auth = false,
        ).let(::parseToken)

    suspend fun login(emailOrUsername: String, password: String): ApiToken =
        postJson(
            "/auth/login",
            JSONObject()
                .put("email", emailOrUsername)
                .put("password", password),
            auth = false,
        ).let(::parseToken)

    suspend fun me(): ApiUser {
        val o = getJson("/auth/me")
        return ApiUser(
            id = o.getInt("id"),
            username = o.getString("username"),
            cityName = o.optString("city_name").takeIf { it.isNotEmpty() && it != "null" },
            streak = o.getInt("streak"),
            totalCheckIns = o.getInt("total_check_ins"),
        )
    }

    suspend fun todayQuest(
        cityName: String,
        centerLat: Double,
        centerLon: Double,
        userLat: Double?,
        userLon: Double?,
    ): ApiQuest {
        val body = JSONObject()
            .put("city_name", cityName)
            .put("center_lat", centerLat)
            .put("center_lon", centerLon)
        if (userLat != null) body.put("user_lat", userLat)
        if (userLon != null) body.put("user_lon", userLon)
        return parseQuest(postJson("/quests/today", body))
    }

    suspend fun myCheckIn(): ApiCheckIn? {
        val raw = getRaw("/quests/today/mine")
        if (raw.isBlank() || raw == "null") return null
        return parseCheckIn(JSONObject(raw))
    }

    suspend fun submitCheckIn(questId: Int, photoUrl: String?): ApiCheckIn {
        val body = JSONObject()
        if (photoUrl != null) body.put("photo_url", photoUrl)
        val o = postJson("/quests/$questId/check-in", body)
        return parseCheckIn(o.getJSONObject("check_in"))
    }

    suspend fun wall(questId: Int): List<ApiWallPhoto> {
        val arr = getArray("/social/wall/$questId")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ApiWallPhoto(
                checkInId = o.getInt("check_in_id"),
                username = o.getString("username"),
                photoUrl = o.optString("photo_url").takeIf { it.isNotEmpty() && it != "null" },
                reactions = o.optInt("reactions", 0),
            )
        }
    }

    suspend fun leaderboard(city: String?): List<ApiLeaderboardEntry> {
        val path = if (city.isNullOrBlank()) {
            "/social/leaderboard"
        } else {
            "/social/leaderboard?city=${java.net.URLEncoder.encode(city, "UTF-8")}"
        }
        val arr = getArray(path)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ApiLeaderboardEntry(
                userId = o.getInt("user_id"),
                username = o.getString("username"),
                streak = o.getInt("streak"),
                totalCheckIns = o.getInt("total_check_ins"),
                rank = o.getInt("rank"),
                checkedToday = o.getBoolean("checked_today"),
            )
        }
    }

    suspend fun history(): List<ApiHistoryItem> {
        val arr = getArray("/quests/history")
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val q = o.optJSONObject("quest") ?: return@mapNotNull null
            val c = o.optJSONObject("check_in") ?: return@mapNotNull null
            ApiHistoryItem(parseQuest(q), parseCheckIn(c))
        }
    }

    suspend fun healthOk(): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$baseUrl/health").openConnection() as HttpURLConnection).apply {
                connectTimeout = 4_000
                readTimeout = 4_000
                requestMethod = "GET"
            }
            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }

    private fun parseToken(o: JSONObject) = ApiToken(
        accessToken = o.getString("access_token"),
        userId = o.getInt("user_id"),
        username = o.getString("username"),
    )

    private fun parseQuest(o: JSONObject) = ApiQuest(
        id = o.getInt("id"),
        date = o.getString("date"),
        cityName = o.getString("city_name"),
        poiName = o.getString("poi_name"),
        poiDescription = o.optString("poi_description"),
        latitude = o.getDouble("latitude"),
        longitude = o.getDouble("longitude"),
        rarity = o.optString("rarity", "COMMON"),
        photoUrl = o.optString("photo_url").takeIf { it.isNotEmpty() && it != "null" },
        distanceMeters = if (o.has("distance_meters") && !o.isNull("distance_meters")) {
            o.getDouble("distance_meters").toFloat()
        } else {
            null
        },
    )

    private fun parseCheckIn(o: JSONObject) = ApiCheckIn(
        id = o.getInt("id"),
        questId = o.getInt("quest_id"),
        userId = o.getInt("user_id"),
        status = o.getString("status"),
        validatedBy = if (o.has("validated_by") && !o.isNull("validated_by")) o.getInt("validated_by") else null,
        submittedAt = o.optString("submitted_at"),
    )

    private suspend fun getJson(path: String): JSONObject =
        JSONObject(getRaw(path))

    private suspend fun getArray(path: String): JSONArray =
        JSONArray(getRaw(path))

    private suspend fun getRaw(path: String): String = withContext(Dispatchers.IO) {
        request("GET", path, null, auth = true)
    }

    private suspend fun postJson(path: String, body: JSONObject, auth: Boolean = true): JSONObject =
        withContext(Dispatchers.IO) {
            JSONObject(request("POST", path, body.toString(), auth))
        }

    private fun request(method: String, path: String, body: String?, auth: Boolean): String {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 20_000
            requestMethod = method
            setRequestProperty("Accept", "application/json")
            if (auth) {
                val token = accessToken
                    ?: throw IllegalStateException("Non authentifié")
                setRequestProperty("Authorization", "Bearer $token")
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
            }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            throw ApiException(code, text.ifBlank { "Erreur HTTP $code" })
        }
        return text
    }
}

class ApiException(val code: Int, message: String) : Exception(message)
