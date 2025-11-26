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
import com.example.projecteandroid.ui.theme.CreatorInfoDialog
import com.example.projecteandroid.ui.theme.TasksScreen
import com.example.projecteandroid.ui.theme.WelcomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn && navController.currentDestination?.route != AppScreens.TASKS_SCREEN) {
            navController.navigate(AppScreens.TASKS_SCREEN) {
                popUpTo(AppScreens.WELCOME_SCREEN) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.WELCOME_SCREEN
    ) {
        composable(AppScreens.WELCOME_SCREEN) {
            WelcomeScreen(
                uiState = uiState,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange, // Paràmetre que faltava
                onLoginClick = viewModel::onLoginClick,
                onShowCreatorDialog = viewModel::onShowCreatorDialog
            )

            if (uiState.isCreatorDialogVisible) {
                CreatorInfoDialog(onDismiss = viewModel::onDismissCreatorDialog)
            }
        }

        composable(AppScreens.TASKS_SCREEN) {
            TasksScreen(
                tasks = uiState.tasks,
                onAddTask = { title, subject, dueDate -> viewModel.addTask(title, subject, dueDate) },
                onToggleTask = viewModel::toggleTaskCompletion,
                onDeleteTask = viewModel::deleteTask
            )
        }
    }
}