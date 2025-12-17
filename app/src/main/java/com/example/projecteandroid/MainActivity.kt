package com.example.projecteandroid


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.projecteandroid.navigation.AppNavigation
import com.example.projecteandroid.ui.theme.ProjecteANDROIDTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: com.example.projecteandroid.presentation.MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.projecteandroid.presentation.MainViewModelFactory(application)
            )
            val uiState by viewModel.uiState.collectAsState()

            ProjecteANDROIDTheme(darkTheme = uiState.isDarkTheme) {
                // La MainActivity ara només conté el gestor de navegació
                AppNavigation(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}
