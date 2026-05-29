package com.alma.climbingtraining.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alma.climbingtraining.ui.flyingloto.FlyingLotoScreen
import com.alma.climbingtraining.ui.home.HomeScreen
import com.alma.climbingtraining.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val FLYING_LOTO = "flying_loto"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph() {
    // saveState / restoreState ensure the back stack survives Activity recreation
    // (e.g. after a language change via AppCompatDelegate.setApplicationLocales).
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToFlyingLoto = {
                    navController.navigate(Routes.FLYING_LOTO) {
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS) {
                        restoreState = true
                    }
                }
            )
        }
        composable(Routes.FLYING_LOTO) {
            FlyingLotoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
