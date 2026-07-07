package com.hbazai.mycarservices.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hbazai.mycarservices.screens.*
import com.hbazai.mycarservices.util.AppPreferences

sealed class Screen(val route: String) {
    object Splash     : Screen("splash")
    object Setup      : Screen("setup")
    object Home       : Screen("home")
    object Reports    : Screen("reports?carId={carId}") {
        fun createRoute(carId: Int = -1) = "reports?carId=$carId"
    }
    object Settings   : Screen("settings")
    object AddCar     : Screen("add_car")
    object EditCar    : Screen("edit_car/{carId}") {
        fun createRoute(carId: Int) = "edit_car/$carId"
    }
    object AddService : Screen("add_service/{carId}") {
        fun createRoute(carId: Int) = "add_service/$carId"
    }
    object EditService : Screen("edit_service/{serviceId}") {
        fun createRoute(serviceId: Int) = "edit_service/$serviceId"
    }
    object OilPrediction : Screen("oil_prediction/{carId}") {
        fun createRoute(carId: Int) = "oil_prediction/$carId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (!AppPreferences.isSetupDone(context)) {
                        navController.navigate(Screen.Setup.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddCar      = { navController.navigate(Screen.AddCar.route) },
                onAddService  = { carId -> navController.navigate(Screen.AddService.createRoute(carId)) },
                onCarClick    = { carId -> navController.navigate(Screen.Reports.createRoute(carId)) },
                onEditCar     = { carId -> navController.navigate(Screen.EditCar.createRoute(carId)) },
                onViewReports = { navController.navigate(Screen.Reports.createRoute()) },
                onSettings    = { navController.navigate(Screen.Settings.route) },
                onPredict     = { carId -> navController.navigate(Screen.OilPrediction.createRoute(carId)) }
            )
        }

        composable(Screen.AddCar.route) {
            AddCarScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route     = Screen.EditCar.route,
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { back ->
            val carId = back.arguments?.getInt("carId") ?: return@composable
            EditCarScreen(
                carId  = carId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.AddService.route,
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { back ->
            val carId = back.arguments?.getInt("carId") ?: return@composable
            AddServiceScreen(
                carId  = carId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.EditService.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { back ->
            val serviceId = back.arguments?.getInt("serviceId") ?: return@composable
            EditServiceScreen(
                serviceId = serviceId,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.Reports.route,
            arguments = listOf(navArgument("carId") {
                type             = NavType.IntType
                defaultValue     = -1
            })
        ) { back ->
            val carId = back.arguments?.getInt("carId") ?: -1
            ReportsScreen(
                initialCarId = if (carId == -1) null else carId,
                onBack       = { navController.popBackStack() },
                onEditService = { serviceId ->
                    navController.navigate(Screen.EditService.createRoute(serviceId))
                }
            )
        }

        composable(
            route     = Screen.OilPrediction.route,
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) {
            OilPredictionScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}