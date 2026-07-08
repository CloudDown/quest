package com.quest.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quest.app.core.QuestRepository
import com.quest.app.core.Rarity
import com.quest.app.ui.CheckInScreen
import com.quest.app.ui.HistoryPanel
import com.quest.app.ui.QuestMapScreen
import com.quest.app.ui.SocialPanel
import com.quest.app.ui.TodayPanel
import com.quest.app.ui.WallPanel
import com.quest.app.ui.theme.Honey
import com.quest.app.ui.theme.QuestTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuestTheme {
                QuestApp()
            }
        }
    }
}

@Composable
fun QuestApp() {
    val context = LocalContext.current
    val repository = QuestRepository.get(context)
    val state by repository.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { repository.refresh() }

    val tabs = listOf("Quest", "Mur", "Social", "Journal")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    var checkInOpen by remember { mutableStateOf(false) }
    var mapOpen by remember { mutableStateOf(false) }
    BackHandler(enabled = checkInOpen || mapOpen) {
        checkInOpen = false
        mapOpen = false
    }

    val isLegendary = state.todayQuest?.poi?.rarity == Rarity.LEGENDARY

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            // Header : logo + ville + streak
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("🌿", style = MaterialTheme.typography.titleLarge)
                    Column {
                        Text("Quest", style = MaterialTheme.typography.titleLarge)
                        state.todayQuest?.let {
                            Text(
                                it.cityName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                // Chip streak, dorée les jours légendaires
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isLegendary) Honey.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "🔥 ${state.streak} jours",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1,
            ) { page ->
                when (page) {
                    0 -> TodayPanel(
                        state = state,
                        onCheckInClick = { checkInOpen = true },
                        onMapClick = { mapOpen = true },
                        onGrantPermission = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                        onRetry = { repository.refresh() },
                    )
                    1 -> WallPanel(state = state)
                    2 -> SocialPanel(state = state)
                    3 -> HistoryPanel(state = state)
                }
            }

            // Barre d'onglets pilule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    val tabColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        animationSpec = tween(250),
                        label = "tabColor",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(tabColor, RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                scope.launch {
                                    pagerState.animateScrollToPage(index, animationSpec = tween(350))
                                }
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Carte plein écran
        AnimatedVisibility(
            visible = mapOpen,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.95f, animationSpec = tween(250)),
        ) {
            state.todayQuest?.let { quest ->
                QuestMapScreen(
                    poi = quest.poi,
                    myLatitude = state.myLatitude,
                    myLongitude = state.myLongitude,
                    onClose = { mapOpen = false },
                )
            }
        }

        // Check-in plein écran
        AnimatedVisibility(
            visible = checkInOpen,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.92f, animationSpec = tween(250)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
            ) {
                CheckInScreen(
                    onSubmit = {
                        repository.submitCheckIn()
                        checkInOpen = false
                    },
                    onClose = { checkInOpen = false },
                )
            }
        }
    }
}
