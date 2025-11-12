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
import com.example.projecteandroid.ui.theme.CreatorInfoDialog
import com.example.projecteandroid.ui.theme.ProjecteANDROIDTheme
import com.example.projecteandroid.ui.theme.WelcomeScreen


class MainActivity : ComponentActivity() {
    // Inyectamos el ViewModel usando la delegación de KTX.
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProjecteANDROIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // 1. Observem l'estat del ViewModel.
                    val uiState by viewModel.uiState.collectAsState()

                    // 2. Passem l'estat i les funcions del ViewModel a la nostra pantalla.
                    WelcomeScreen(
                        uiState = uiState,
                        onUsernameChange = viewModel::onUsernameChange, // Passem la referència a la funció
                        onLoginClick = viewModel::onLoginClick,
                        onShowCreatorDialog = viewModel::onShowCreatorDialog,
                        modifier = Modifier.padding(innerPadding)
                    )

                    // 3. Mostrem el diàleg si l'estat ho indica.
                    if (uiState.isCreatorDialogVisible) {
                        CreatorInfoDialog(onDismiss = viewModel::onDismissCreatorDialog)
                    }
                }
            }
        }
    }
}
