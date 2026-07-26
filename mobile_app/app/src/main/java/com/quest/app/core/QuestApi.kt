package com.quest.app.core

import com.quest.app.BuildConfig

/**
 * Point d'entrée HTTP vers le backend Quest (`server/`).
 * L'URL vient de `mobile_app/local.properties` → `quest.api.base.url`
 * (voir `configure-device-api.sh`).
 *
 * Pour l'instant le repository local reste la source UI ; ce client
 * prépare le bascule social / lieu du jour côté serveur.
 */
object QuestApi {
    val baseUrl: String = BuildConfig.API_BASE_URL

    fun healthUrl(): String = "$baseUrl/health"
    fun docsHint(): String = "$baseUrl/docs"
}
