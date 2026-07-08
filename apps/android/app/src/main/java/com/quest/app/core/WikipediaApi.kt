package com.quest.app.core

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** POI brut renvoyé par Wikipedia, avant transformation en [Poi]. */
data class WikiPlace(
    val pageId: Long,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?,
)

/**
 * Client Wikipedia minimaliste (geosearch + extraits + photos).
 * Aucune clé API requise. HttpURLConnection + JSONObject : pas de dépendance réseau.
 */
object WikipediaApi {

    /** fr.wikipedia si le téléphone est en français, sinon en.wikipedia. */
    private val lang: String
        get() = if (Locale.getDefault().language == "fr") "fr" else "en"

    /**
     * Les lieux notables autour d'un point (rayon max 10 km, limite API).
     * Retourne les pages avec coordonnées, extrait d'intro et photo.
     */
    suspend fun placesAround(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 10_000,
        limit: Int = 50,
    ): List<WikiPlace> = withContext(Dispatchers.IO) {
        // 1. geosearch : titres + coordonnées des pages autour du point
        val geoUrl = "https://$lang.wikipedia.org/w/api.php" +
            "?action=query&list=geosearch" +
            "&gscoord=$latitude%7C$longitude" +
            "&gsradius=${radiusMeters.coerceAtMost(10_000)}" +
            "&gslimit=$limit&format=json"
        val geoJson = JSONObject(fetch(geoUrl))
        val results = geoJson.getJSONObject("query").getJSONArray("geosearch")
        if (results.length() == 0) return@withContext emptyList()

        val coords = mutableMapOf<Long, Pair<Double, Double>>()
        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            coords[item.getLong("pageid")] =
                item.getDouble("lat") to item.getDouble("lon")
        }

        // 2. extraits + photos pour toutes les pages (par lots de 50 max)
        val pageIds = coords.keys.joinToString("%7C")
        val detailUrl = "https://$lang.wikipedia.org/w/api.php" +
            "?action=query&pageids=$pageIds" +
            "&prop=extracts%7Cpageimages" +
            "&exintro=1&explaintext=1&exsentences=3&exlimit=max" +
            "&piprop=thumbnail&pithumbsize=1000&pilimit=max" +
            "&format=json"
        val detailJson = JSONObject(fetch(detailUrl))
        val pages = detailJson.getJSONObject("query").getJSONObject("pages")

        coords.mapNotNull { (pageId, latLon) ->
            val page = pages.optJSONObject(pageId.toString()) ?: return@mapNotNull null
            val extract = page.optString("extract").trim()
            if (extract.isEmpty()) return@mapNotNull null
            WikiPlace(
                pageId = pageId,
                title = page.optString("title"),
                description = extract,
                latitude = latLon.first,
                longitude = latLon.second,
                photoUrl = page.optJSONObject("thumbnail")?.optString("source"),
            )
        }
    }

    /** URL de la page Wikipedia d'un lieu (pour un lien « en savoir plus »). */
    fun pageUrl(title: String): String =
        "https://$lang.wikipedia.org/wiki/${URLEncoder.encode(title.replace(' ', '_'), "UTF-8")}"

    private fun fetch(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "QuestApp/0.1 (Android)")
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }
}
