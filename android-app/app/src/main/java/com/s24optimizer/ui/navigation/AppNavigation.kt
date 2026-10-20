package com.s24optimizer.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.ui.*
import com.s24optimizer.ui.screens.*

enum class Screen(
    val route: String,
    val labelEn: String,
    val labelIt: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
) {
    HOME("home", "Home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    DIAGNOSTICS("diagnostics", "Diag", "Diag", Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
    OPTIMIZE("optimize", "Optimize", "Ottimizza", Icons.Filled.Tune, Icons.Outlined.Tune),
    APPS("apps", "Apps", "App", Icons.Filled.Apps, Icons.Outlined.Apps),
    LOGS("logs", "Logs", "Log", Icons.Filled.Terminal, Icons.Outlined.Terminal),
}

@Composable
fun AppNavigation(
    italian: Boolean,
    onToggleLanguage: () -> Unit,
    executor: AdbExecutor,
    shizukuStatus: Boolean,
    appliedStates: Map<String, Boolean>,
    onAppliedStatesChanged: (Map<String, Boolean>) -> Unit,
    log: String,
    onLog: (String) -> Unit,
    onClearLog: () -> Unit,
    isRunning: Boolean,
    onRunningChanged: (Boolean) -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.HOME.route

    Scaffold(
        containerColor = SurfaceDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
            ) {
                Screen.entries.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = screen.labelEn,
                            )
                        },
                        label = {
                            Text(
                                if (italian) screen.labelIt else screen.labelEn,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricBlue,
                            selectedTextColor = ElectricBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = ElectricBlue.copy(alpha = 0.12f),
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.HOME.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 12 } },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(150)) },
        ) {
            composable(Screen.HOME.route) {
                HomeScreen(
                    italian = italian,
                    onToggleLanguage = onToggleLanguage,
                    executor = executor,
                    shizukuStatus = shizukuStatus,
                    appliedStates = appliedStates,
                    isRunning = isRunning,
                    onLog = onLog,
                )
            }
            composable(Screen.DIAGNOSTICS.route) {
                DiagnosticsScreen(
                    italian = italian,
                    executor = executor,
                    shizukuStatus = shizukuStatus,
                    appliedStates = appliedStates,
                    onLog = onLog,
                )
            }
            composable(Screen.OPTIMIZE.route) {
                OptimizeScreen(
                    italian = italian,
                    executor = executor,
                    shizukuStatus = shizukuStatus,
                    appliedStates = appliedStates,
                    onAppliedStatesChanged = onAppliedStatesChanged,
                    isRunning = isRunning,
                    onRunningChanged = onRunningChanged,
                    onLog = onLog,
                )
            }
            composable(Screen.APPS.route) {
                AppsScreen(
                    italian = italian,
                    executor = executor,
                    appliedStates = appliedStates,
                    onAppliedStatesChanged = onAppliedStatesChanged,
                    onLog = onLog,
                )
            }
            composable(Screen.LOGS.route) {
                LogsScreen(
                    italian = italian,
                    log = log,
                    onClearLog = onClearLog,
                )
            }
        }
    }
}
