package com.example.projecteandroid.presentation

import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import com.example.projecteandroid.data.GreetingModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel: Contiene el estado de la UI y la lógica de negocio.
class MainViewModel : ViewModel() {

    // _uiState es privado para que solo el ViewModel pueda modificarlo.
    // Usamos MutableStateFlow para que el estado pueda cambiar.
    private val _uiState = MutableStateFlow(GreetingModel(message = "Hello Android!"))

    // uiState es público y de solo lectura (StateFlow).
    // La UI observará este flujo para obtener los datos.
    val uiState: StateFlow<GreetingModel> = _uiState.asStateFlow()

    // Lógica de negocio: una función para cambiar el saludo.
    fun updateGreeting() {
        _uiState.update { currentState ->
            currentState.copy(message = "Hello MVVM!")
        }
    }
}