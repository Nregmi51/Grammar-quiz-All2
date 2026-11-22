package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.GrammarViewModel
import com.example.ui.screens.ContentScreen
import com.example.ui.screens.IntroScreen
import com.example.ui.screens.PlayWatchScreen
import com.example.ui.theme.DeepBlueBanner
import com.example.ui.theme.DeepBlueInk
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GrammarCraftTheme
import com.example.ui.theme.GreenBlueBackdropBrush
import com.example.ui.theme.OnDeepBlueText
import com.example.ui.theme.PlateYellow

private const val ROUTE_INTRO = "intro"
private const val ROUTE_CONTENT = "content"
private const val ROUTE_PLAY_WATCH = "play_watch"

private data class NavSection(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navSections = listOf(
    NavSection(
        route = ROUTE_INTRO,
        label = "Intro",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_intro"
    ),
    NavSection(
        route = ROUTE_CONTENT,
        label = "Content",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        testTag = "nav_content"
    ),
    NavSection(
        route = ROUTE_PLAY_WATCH,
        label = "Play / Watch",
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle,
        testTag = "nav_play_watch"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrammarCraftTheme {
                GrammarCraftApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarCraftApp(viewModel: GrammarViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigateToSection: (String) -> Unit = { route ->
        if (route != ROUTE_PLAY_WATCH && uiState.isWatchAutoPlay) {
            viewModel.stopWatchAutoPlay()
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val currentSectionTitle = when (currentDestination?.route) {
        ROUTE_CONTENT -> "Grammar Content"
        ROUTE_PLAY_WATCH -> "Play or Watch"
        else -> "Grammar Craft"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenBlueBackdropBrush)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 640.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = DeepBlueBanner.copy(alpha = 0.94f),
                        contentColor = OnDeepBlueText
                    ) {
                        navSections.forEach { section ->
                            val selected = currentDestination?.hierarchy?.any { it.route == section.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToSection(section.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) section.selectedIcon else section.unselectedIcon,
                                        contentDescription = section.label
                                    )
                                },
                                label = { Text(section.label) },
                                modifier = Modifier.testTag(section.testTag),
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = DeepBlueInk,
                                    selectedTextColor = PlateYellow,
                                    indicatorColor = PlateYellow,
                                    unselectedIconColor = OnDeepBlueText.copy(alpha = 0.78f),
                                    unselectedTextColor = OnDeepBlueText.copy(alpha = 0.78f)
                                )
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                androidx.compose.foundation.layout.Column(
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = currentSectionTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = OnDeepBlueText
                                    )
                                    Text(
                                        text = "Developed by NP Regmi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldAccent
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = DeepBlueBanner.copy(alpha = 0.92f),
                                titleContentColor = OnDeepBlueText
                            )
                        )
                    },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(
                                containerColor = DeepBlueBanner.copy(alpha = 0.96f),
                                contentColor = OnDeepBlueText,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                navSections.forEach { section ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == section.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToSection(section.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) section.selectedIcon else section.unselectedIcon,
                                                contentDescription = section.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = section.label,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        modifier = Modifier.testTag(section.testTag),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = DeepBlueInk,
                                            selectedTextColor = GoldAccent,
                                            indicatorColor = PlateYellow,
                                            unselectedIconColor = OnDeepBlueText.copy(alpha = 0.78f),
                                            unselectedTextColor = OnDeepBlueText.copy(alpha = 0.78f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_INTRO,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(ROUTE_INTRO) {
                            IntroScreen(
                                uiState = uiState,
                                contentPadding = innerPadding,
                                onGitHubUrlChanged = viewModel::onGitHubUrlChanged,
                                onSyncGitHub = viewModel::syncGitHubRepo,
                                onResetGitHub = viewModel::clearGitHubImports,
                                onDismissSyncBanner = viewModel::dismissSyncBanner,
                                onNavigateToContent = { navigateToSection(ROUTE_CONTENT) },
                                onNavigateToPlayWatch = { navigateToSection(ROUTE_PLAY_WATCH) }
                            )
                        }

                        composable(ROUTE_CONTENT) {
                            ContentScreen(
                                uiState = uiState,
                                contentPadding = innerPadding,
                                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                                onSelectTopicFilter = viewModel::selectTopicFilter,
                                onSelectCategoryFilter = viewModel::selectCategoryFilter,
                                onOpenItemPlayWatch = { itemId ->
                                    viewModel.openItemInPlayWatch(itemId)
                                    navigateToSection(ROUTE_PLAY_WATCH)
                                },
                                onOpenTopicDeckPlayWatch = { topic ->
                                    viewModel.openTopicDeckInPlayWatch(topic)
                                    navigateToSection(ROUTE_PLAY_WATCH)
                                },
                                onToggleCompleted = viewModel::toggleCompleted
                            )
                        }

                        composable(ROUTE_PLAY_WATCH) {
                            PlayWatchScreen(
                                uiState = uiState,
                                contentPadding = innerPadding,
                                onSelectItem = viewModel::openItemInPlayWatch,
                                onExitTopicDeck = viewModel::exitTopicDeckToSingleItem,
                                onPreviousItem = viewModel::selectPreviousPlayItem,
                                onNextItem = viewModel::selectNextPlayItem,
                                onToggleWatchAutoPlay = viewModel::toggleWatchAutoPlay,
                                onAdjustZoom = viewModel::adjustTextZoom,
                                onToggleCompleted = viewModel::toggleCompleted
                            )
                        }
                    }
                }
            }
        }
    }
}
