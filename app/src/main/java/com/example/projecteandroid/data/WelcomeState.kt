package com.example.projecteandroid.data

// Model que representa l'estat de la UI
data class WelcomeState (
    val appTitle: String = "Gestor Escolar",
    val appDescription: String = "Planifica les teves tasques i lliuraments acadèmics de manera senzilla i eficient.",
    val username: String = "",
    val password: String = "", // Camp per a la contrasenya
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val tasks: List<Task> = emptyList(),
    val loginError: String? = null, // Camp per al missatge d'error
    val isDarkTheme: Boolean = false // Estat del tema (clar/fosc)
)