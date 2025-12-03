package com.example.projecteandroid.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.projecteandroid.presentation.NavigationEvent
import com.example.projecteandroid.data.AppDatabase
import com.example.projecteandroid.data.Task
import com.example.projecteandroid.data.TaskStatus
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

    // Funcio per a iniciar sessió
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

    // Funcio per a registrar un nou usuari
    fun onRegisterClick() {
        viewModelScope.launch {
            val currentState = _uiState.value
            // Validació bàsica
            if (currentState.username.isNotBlank() && currentState.password.isNotBlank()) {
                val existingUser = database.userDao().getUserByUsername(currentState.username)
                if (existingUser == null) {
                    // L'usuari no existeix, el podem crear
                    val newUser = User(username = currentState.username, password = currentState.password)
                    database.userDao().insertUser(newUser)
                    // Opcional: Iniciar sessió directament després de registrar-se
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            currentUser = newUser,
                            loginError = null,
                            password = ""
                        )
                    }
                    _navigationEvent.send(NavigationEvent.NavigateToTasks)
                } else {
                    _uiState.update { it.copy(loginError = "El nom d'usuari ja existeix") }
                }
            } else {
                _uiState.update { it.copy(loginError = "El nom d'usuari i la contrasenya no poden estar buits") }
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
        viewModelScope.launch {
            val currentUser = _uiState.value.currentUser
            if (currentUser != null && title.isNotBlank()) {
                val newTask = Task(
                    userId = currentUser.id,
                    title = title,
                    subject = subject,
                    dueDate = dueDate,
                    status = TaskStatus.PENDING // Totes les tasques noves comencen com a pendents
                )
                database.taskDao().insertTask(newTask)
            }
        }
    }

    // NOVA funció per actualitzar una tasca existent (per editar)
    fun updateTask(task: Task) {
        viewModelScope.launch {
            database.taskDao().updateTask(task)
        }
    }

    // NOVA funció per canviar l'estat d'una tasca
    fun moveTask(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = newStatus)
            database.taskDao().updateTask(updatedTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            database.taskDao().deleteTask(task)
        }
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