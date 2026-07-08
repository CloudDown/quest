package com.quest.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quest.app.ui.theme.Lime
import com.quest.app.ui.theme.Sun

/**
 * Écran check-in photo (overlay plein écran).
 * TODO: CameraX + vérification GPS au POI.
 */
@Composable
fun CheckInScreen(
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Brush.linearGradient(listOf(Lime, Sun)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("📸", style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(24.dp))
        Text("Check-in photo", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Text(
            "La caméra s'ouvrira ici.\nPas de validation depuis son canapé.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Lime,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Soumettre (démo)", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClose) {
            Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
