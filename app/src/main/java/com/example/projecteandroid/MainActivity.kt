package com.example.projecteandroid


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.projecteandroid.navigation.AppNavigation
import com.example.projecteandroid.ui.theme.ProjecteANDROIDTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProjecteANDROIDTheme {
                // La MainActivity ara només conté el gestor de navegació
                AppNavigation()
            }
        }
    }
}
