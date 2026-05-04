package com.hbazai.mycarservices.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hbazai.mycarservices.screens.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val arrowBackIcon
    @Composable get() = Icons.AutoMirrored.Filled.ArrowBack

sealed class Screen(val route: String) {
    object Splash      : Screen("splash")
    object Home        : Screen("home")
    object AddService  : Screen("add_service?carId={carId}&serviceId={serviceId}") {
        fun createRoute(carId: Int = -1, serviceId: Int = -1) =
            "add_service?carId=$carId&serviceId=$serviceId"
    }
    object Reports     : Screen("reports")
    object Settings    : Screen("settings")
    object AddCar      : Screen("add_car")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddCar = { navController.navigate(Screen.AddCar.route) },
                onAddService = { carId ->
                    navController.navigate(Screen.AddService.createRoute(carId = carId))
                },
                onViewReports = { navController.navigate(Screen.Reports.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.AddCar.route) {
            AddCarScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddService.route) { backStackEntry ->
            val carId     = backStackEntry.arguments?.getString("carId")?.toIntOrNull() ?: -1
            val serviceId = backStackEntry.arguments?.getString("serviceId")?.toIntOrNull() ?: -1
            AddServiceScreen(
                carId     = carId,
                serviceId = serviceId,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}