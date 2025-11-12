package com.example.projecteandroid.data

// Model representen les nostres dades
data class WelcomeState (
    val appTitle: String = "Gestor Escolar",
    val appDescription: String = "Planifica les teves tasques i lliuraments acadèmics de manera senzilla i eficient.",
    val username: String = "",
    val password: String = "", // Camp per a la contrasenya
    val loginError: String? = null, //Missatge d'error per al login
    val isCreatorDialogVisible: Boolean = false // Estat per controlar la visibilitat del diàleg
)