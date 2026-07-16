package com.quest.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.quest.app.R
import com.quest.app.core.Poi
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

/**
 * Carte OpenStreetMap centrée sur le lieu du jour.
 * [interactive] false pour l'aperçu dans la page (le pager garde ses gestes),
 * true pour le plein écran.
 */
@Composable
fun QuestMap(
    poi: Poi,
    myLatitude: Double?,
    myLongitude: Double?,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> createMapView(context, interactive) },
        update = { mapView ->
            mapView.overlays.removeAll { it is Marker || it is MyPositionOverlay }

            val pinDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.pin_quest)
            val marker = Marker(mapView).apply {
                position = GeoPoint(poi.latitude, poi.longitude)
                title = poi.name
                icon = pinDrawable
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)

            if (myLatitude != null && myLongitude != null) {
                mapView.overlays.add(MyPositionOverlay(GeoPoint(myLatitude, myLongitude)))
            }

            mapView.controller.setCenter(GeoPoint(poi.latitude, poi.longitude))
            mapView.invalidate()
        },
    )
}

/** Carte plein écran avec bouton fermer. */
@Composable
fun QuestMapScreen(
    poi: Poi,
    myLatitude: Double?,
    myLongitude: Double?,
    onClose: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        QuestMap(
            poi = poi,
            myLatitude = myLatitude,
            myLongitude = myLongitude,
            modifier = Modifier.fillMaxSize(),
            interactive = true,
        )

        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
            ) {
                Text(
                    poi.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

/** Point « moi » : disque lime cerclé de vert forêt. */
private class MyPositionOverlay(private val point: GeoPoint) : Overlay() {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#9FE870")
        style = Paint.Style.FILL
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#163300")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val screenPoint = android.graphics.Point()
        mapView.projection.toPixels(point, screenPoint)
        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), 22f, fill)
        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), 22f, ring)
    }
}

private fun createMapView(context: Context, interactive: Boolean): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(interactive)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        if (!interactive) {
            // Aperçu figé : aucun geste, le tap est géré par le parent Compose
            setOnTouchListener { _, _ -> true }
        }
        controller.setZoom(if (interactive) 15.5 else 14.5)
    }
}
