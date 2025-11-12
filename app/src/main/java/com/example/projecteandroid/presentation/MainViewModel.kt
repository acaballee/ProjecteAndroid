package com.example.projecteandroid.presentation

import androidx.lifecycle.ViewModel
import com.example.projecteandroid.data.WelcomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel: Contiene el estado de la UI y la lógica de negocio.
class MainViewModel : ViewModel() {

    // L'estat inicial ara utilitza el nostre nou model WelcomeState.
    private val _uiState = MutableStateFlow(WelcomeState())
    val uiState: StateFlow<WelcomeState> = _uiState.asStateFlow()

    // --- Lògica de Negoci (Events de la UI) ---

    // Funció que s'executa quan l'usuari escriu al camp de text.
    fun onUsernameChange(newUsername: String) {
        _uiState.update { currentState ->
            currentState.copy(username = newUsername)
        }
    }

    // Funció per fer el login (de moment, només mostra un missatge).
    fun onLoginClick() {
        val currentUser = _uiState.value.username
        if (currentUser.isNotBlank()) {
            println("Login intentat per l'usuari: $currentUser")
            // Aquí anirà la lògica de navegació cap a la pantalla principal de tasques.
        }
    }

    // Funció per mostrar el diàleg d'informació del creador.
    fun onShowCreatorDialog() {
        _uiState.update { it.copy(isCreatorDialogVisible = true) }
    }

    // Funció per amagar el diàleg.
    fun onDismissCreatorDialog() {
        _uiState.update { it.copy(isCreatorDialogVisible = false) }
    }
}