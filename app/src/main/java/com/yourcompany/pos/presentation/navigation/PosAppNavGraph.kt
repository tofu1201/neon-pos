package com.yourcompany.pos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourcompany.pos.presentation.pos.PosEvent
import com.yourcompany.pos.presentation.pos.PosScreen
import com.yourcompany.pos.presentation.pos.PosViewModel
import com.yourcompany.pos.presentation.settings.SettingsScreen
import com.yourcompany.pos.presentation.pos.CheckoutScreen

@Composable
fun PosAppNavGraph(
    posViewModel: PosViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    val state by posViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") {
            PosScreen(
                state = state,
                onEvent = { event ->
                    when (event) {
                        is PosEvent.NavigateToCheckout -> navController.navigate("checkout")
                        is PosEvent.NavigateToSettings -> navController.navigate("settings")
                        else -> posViewModel.onEvent(event)
                    }
                }
            )
        }

        composable("checkout") {
            CheckoutScreen(
                state = state,
                onEvent = posViewModel::onEvent,
                onBack = { navController.popBackStack() },
                onCheckoutComplete = { navController.popBackStack("home", inclusive = false) }
            )
        }

        composable("settings") {
            SettingsScreen(
                state = state,
                onEvent = posViewModel::onEvent,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
