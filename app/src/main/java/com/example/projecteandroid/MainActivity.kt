package com.example.projecteandroid


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.projecteandroid.presentation.MainViewModel
import com.example.projecteandroid.ui.theme.GreetingScreen
import com.example.projecteandroid.ui.theme.ProjecteANDROIDTheme


class MainActivity : ComponentActivity() {
    // Inyectamos el ViewModel usando la delegación de KTX.
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProjecteANDROIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // 1. Observamos el estado del ViewModel.
                    // `collectAsState` convierte el StateFlow en un State de Compose.
                    val uiState by viewModel.uiState.collectAsState()

                    // 2. Pasamos el estado actual a nuestra pantalla Composable.
                    GreetingScreen(
                        uiState = uiState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
