package com.example.projecteandroid.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TasksScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de Tasques (Buida de moment)")
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    ProjecteANDROIDTheme {
        TasksScreen()
    }
}