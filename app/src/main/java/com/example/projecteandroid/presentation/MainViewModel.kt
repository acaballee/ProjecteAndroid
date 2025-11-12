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
        _uiState.update { it.copy(username = newUsername, loginError = null) }
    }
    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, loginError = null) }
    }

    // Funció que retorna true si el login és correcte, o false si no ho és.
    fun onLoginClick(): Boolean {
        val currentState = _uiState.value
        return if (currentState.username == "admin" && currentState.password == "admin") {
            _uiState.update { it.copy(loginError = null) }
            true // Login correcte
        } else {
            _uiState.update { it.copy(loginError = "Usuari o contrasenya incorrectes") }
            false // Login incorrecte
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