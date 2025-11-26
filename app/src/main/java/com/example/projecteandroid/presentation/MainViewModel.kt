package com.example.projecteandroid.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.projecteandroid.presentation.NavigationEvent
import com.example.projecteandroid.data.AppDatabase
import com.example.projecteandroid.data.Task
import com.example.projecteandroid.data.User
import com.example.projecteandroid.data.WelcomeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class MainViewModel(private val database: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeState())
    val uiState = _uiState.asStateFlow()

    // Canal per a esdeveniments de navegació ---
    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        // Al iniciar, observar el último usuario de la base de datos
        viewModelScope.launch {
            database.userDao().getLastUser().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(username = user.username, isLoggedIn = true, currentUser = user, password = "") }
                    observeTasks(user.id)
                }
            }
        }
    }

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, loginError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, loginError = null) }
    }

    fun onLoginClick() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.username == "admin" && currentState.password == "admin") {
                val user = User(username = currentState.username, password = currentState.password)
                database.userDao().insertUser(user)
                // Abans de navegar, actualitzem l'estat per reflectir que la sessió és activa.
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        currentUser = user,
                        loginError = null,
                        password = "" // Opcional: buidem la contrasenya de l'estat per seguretat
                    )
                }
                _navigationEvent.send(NavigationEvent.NavigateToTasks)
            } else {
                _uiState.update { it.copy(loginError = "Usuari o contrasenya incorrectes") }
            }
        }
    }

    private fun observeTasks(userId: Int) {
        viewModelScope.launch {
            database.taskDao().getTasksForUser(userId).collectLatest { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
    }

    // --- Funcions per gestionar les tasques ---

    fun addTask(title: String, subject: String, dueDate: String) {
        val userId = _uiState.value.currentUser?.id ?: return
        val newTask = Task(userId = userId, title = title, subject = subject, dueDate = dueDate)
        viewModelScope.launch {
            database.taskDao().insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            database.taskDao().updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            database.taskDao().deleteTask(task)
        }
    }

    // --- Gestió de diàlegs i altres ---

    fun onShowCreatorDialog() {
        _uiState.update { it.copy(isCreatorDialogVisible = true) }
    }

    fun onDismissCreatorDialog() {
        _uiState.update { it.copy(isCreatorDialogVisible = false) }
    }

    // Dins de la classe MainViewModel
    fun logout() {
        viewModelScope.launch {
            // Obtenim l'usuari actual de l'estat
            val currentUser = _uiState.value.currentUser
            if (currentUser != null) {
                // Esborrem l'usuari de la base de dades
                database.userDao().deleteUser(currentUser)
            }
            // Actualitzem l'estat de la UI per reflectir que no hi ha sessió iniciada
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    currentUser = null,
                    username = "",
                    password = "",
                    tasks = emptyList() // Buidem la llista de tasques
                )
            }
            // Enviem un esdeveniment per tornar al login ---
            _navigationEvent.send(NavigationEvent.NavigateToLogin)
        }
    }
}

// --- NOU: Afegeix aquesta classe segellada (sealed class) al final del fitxer ---
sealed class NavigationEvent {
    data object NavigateToTasks : NavigationEvent()
    data object NavigateToLogin : NavigationEvent()
}

// Factory para poder pasar el contexto de la aplicación al ViewModel
class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(AppDatabase.getDatabase(application)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}