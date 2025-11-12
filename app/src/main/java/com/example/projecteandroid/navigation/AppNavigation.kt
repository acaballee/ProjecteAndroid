package com.example.projecteandroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projecteandroid.presentation.MainViewModel
import com.example.projecteandroid.ui.theme.CreatorInfoDialog
import com.example.projecteandroid.ui.theme.TasksScreen
import com.example.projecteandroid.ui.theme.WelcomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel() // Instància del ViewModel compartida

    NavHost(
        navController = navController,
        startDestination = AppScreens.WELCOME_SCREEN
    ) {
        // Pantalla de Benvinguda / Login
        composable(AppScreens.WELCOME_SCREEN) {
            val uiState by viewModel.uiState.collectAsState()

            WelcomeScreen(
                uiState = uiState,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = {
                    if (viewModel.onLoginClick()) {
                        // Navega a la pantalla de tasques si el login és correcte
                        navController.navigate(AppScreens.TASKS_SCREEN)
                    }
                },
                onShowCreatorDialog = viewModel::onShowCreatorDialog
            )

            if (uiState.isCreatorDialogVisible) {
                CreatorInfoDialog(onDismiss = viewModel::onDismissCreatorDialog)
            }
        }

        // Pantalla de Tasques
        composable(AppScreens.TASKS_SCREEN) {
            TasksScreen()
        }
    }
}