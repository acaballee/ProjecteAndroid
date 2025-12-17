package com.example.projecteandroid.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projecteandroid.presentation.MainViewModel
import com.example.projecteandroid.presentation.MainViewModelFactory
import com.example.projecteandroid.presentation.NavigationEvent
import com.example.projecteandroid.ui.theme.CreatorInfoDialog
import com.example.projecteandroid.ui.theme.TasksScreen
import com.example.projecteandroid.ui.theme.WelcomeScreen

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    uiState: com.example.projecteandroid.data.WelcomeState
) {
    val navController = rememberNavController()
    // viewModel i uiState venen de fora per compartir l'estat del tema

    // LaunchedEffect per escoltar els esdeveniments de navegació ---
    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToTasks -> {
                    navController.navigate(AppScreens.TASKS_SCREEN) {
                        popUpTo(AppScreens.WELCOME_SCREEN) { inclusive = true }
                    }
                }
                is NavigationEvent.NavigateToLogin -> {
                    navController.navigate(AppScreens.WELCOME_SCREEN) {
                        // Neteja la pila fins a l'inici per no tornar a la pantalla de tasques
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        // Si hi ha sessió iniciada, la pantalla d'inici és la de tasques, si no, la de benvinguda
        startDestination = if (uiState.isLoggedIn) AppScreens.TASKS_SCREEN else AppScreens.WELCOME_SCREEN
    ) {
        composable(AppScreens.WELCOME_SCREEN) {
            // El contingut de WelcomeScreen no canvia, però ara onLoginClick no navega directament
            WelcomeScreen(
                uiState = uiState,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = viewModel::onLoginClick, // Només crida la funció del ViewModel
                onRegisterClick = viewModel::onRegisterClick,
                onShowCreatorDialog = {} // Actualitzat
            )
        }

        composable(AppScreens.TASKS_SCREEN) {
            // El contingut de TasksScreen només necessita cridar a logout
            TasksScreen(
                tasks = uiState.tasks,
                username = uiState.username, // Passem l'usuari
                isDarkTheme = uiState.isDarkTheme, // Passem l'estat del tema
                onThemeChange = viewModel::toggleTheme, // Passem la funció de canvi de tema
                onAddTask = viewModel::addTask,
                onUpdateTask = viewModel::updateTask,
                onMoveTask = viewModel::moveTask,
                onDeleteTask = viewModel::deleteTask,
                onLogout = viewModel::logout // <-- Canviat de onNavigateBack a onLogout per claredat
            )
        }
    }
}

