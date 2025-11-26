package com.example.projecteandroid.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.projecteandroid.data.AppDatabase
import com.example.projecteandroid.data.Task
import com.example.projecteandroid.data.User
import com.example.projecteandroid.data.WelcomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val database: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeState())
    val uiState = _uiState.asStateFlow()

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
        val state = _uiState.value
        if (state.username.isNotBlank() && state.password.isNotBlank()) {
            viewModelScope.launch {
                val userToSave = User(username = state.username)
                database.userDao().insertUser(userToSave)
            }
        } else {
            _uiState.update { it.copy(loginError = "El nom d'usuari i la contrasenya no poden estar buits.") }
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