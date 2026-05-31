package com.alma.climbingtraining.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alma.climbingtraining.ui.flyingloto.FlyingLotoScreen
import com.alma.climbingtraining.ui.home.HomeScreen
import com.alma.climbingtraining.ui.randomexercise.RandomExerciseScreen
import com.alma.climbingtraining.ui.settings.SettingsScreen
import com.alma.climbingtraining.ui.warmup.WarmupScreen

object Routes {
    const val HOME = "home"
    const val FLYING_LOTO = "flying_loto"
    const val RANDOM_EXERCISE = "random_exercise"
    const val WARMUP = "warmup"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToFlyingLoto = {
                    navController.navigate(Routes.FLYING_LOTO) { restoreState = true }
                },
                onNavigateToRandomExercise = {
                    navController.navigate(Routes.RANDOM_EXERCISE) { restoreState = true }
                },
                onNavigateToWarmup = {
                    navController.navigate(Routes.WARMUP) { restoreState = true }
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS) { restoreState = true }
                }
            )
        }
        composable(Routes.FLYING_LOTO) {
            FlyingLotoScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.RANDOM_EXERCISE) {
            RandomExerciseScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.WARMUP) {
            WarmupScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
