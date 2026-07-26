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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import com.quest.app.core.QuestRepository
import com.quest.app.core.Rarity
import com.quest.app.core.ThemeMode
import com.quest.app.ui.CheckInScreen
import com.quest.app.ui.ProfilePanel
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
            val context = LocalContext.current
            val repository = QuestRepository.get(context)
            val state by repository.uiState.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (state.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            QuestTheme(darkTheme = darkTheme) {
                QuestApp(repository = repository)
            }
        }
    }
}

@Composable
fun QuestApp(repository: QuestRepository) {
    val state by repository.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { repository.refresh() }

    fun requestLocation() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    val tabs = listOf("Quest", "Mur", "Amis", "Profil")
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
            // Header : marque + ville + streak (doré les jours légendaires)
            Column(
                modifier = Modifier.fillMaxWidth().background(
                    if (isLegendary) Honey.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Quest",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            state.todayQuest?.cityName ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isLegendary) Honey.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            "🔥 ${state.streak}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
                        onGrantPermission = { requestLocation() },
                        onRetry = { repository.refresh() },
                    )
                    1 -> WallPanel(state = state)
                    2 -> SocialPanel(state = state)
                    3 -> ProfilePanel(
                        state = state,
                        onThemeModeChange = { repository.setThemeMode(it) },
                        onGrantPermission = { requestLocation() },
                    )
                }
            }

            // Navigation — onglet actif = trait vert sous le label
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    tabs.forEachIndexed { index, label ->
                        val selected = pagerState.currentPage == index
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index, animationSpec = tween(300))
                                    }
                                }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                    ),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = mapOpen,
            enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200)),
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

        AnimatedVisibility(
            visible = checkInOpen,
            enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.94f, animationSpec = tween(250)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.94f, animationSpec = tween(200)),
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
