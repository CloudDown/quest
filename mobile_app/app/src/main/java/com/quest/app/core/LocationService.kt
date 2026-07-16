package com.quest.app.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** La ville détectée : nom + centre géographique. */
data class City(
    val name: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
)

/** Position + géocodage de la ville. */
class LocationService(private val context: Context) {

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Position actuelle (ou dernière connue en secours). */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): Location? {
        if (!hasPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        return try {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                ?: client.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Ville correspondant à une position. Le centre-ville vient du géocodage
     * inverse (le Geocoder renvoie le centroïde de la localité), pour que
     * tous les habitants d'une même ville partagent le même point de référence.
     */
    suspend fun cityAt(latitude: Double, longitude: Double): City? =
        withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val address = geocodeCompat(geocoder, latitude, longitude) ?: return@withContext null
            val cityName = address.locality
                ?: address.subAdminArea
                ?: address.adminArea
                ?: return@withContext null

            // Géocodage direct du nom pour obtenir le centre canonique de la ville
            val center = try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(cityName, 1)?.firstOrNull()
            } catch (e: Exception) {
                null
            }

            City(
                name = cityName,
                centerLatitude = center?.latitude ?: latitude,
                centerLongitude = center?.longitude ?: longitude,
            )
        }

    private suspend fun geocodeCompat(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double,
    ): android.location.Address? {
        return if (Build.VERSION.SDK_INT >= 33) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    continuation.resume(addresses.firstOrNull())
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }
}
