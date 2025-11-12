package com.example.projecteandroid.ui.theme


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.projecteandroid.data.GreetingModel
import com.example.projecteandroid.ui.theme.ProjecteANDROIDTheme

// View: La pantalla que muestra los datos del ViewModel.
// No tiene lógica, solo recibe el estado y lo muestra.
@Composable
fun GreetingScreen(
    uiState: GreetingModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = uiState.message
        )
    }
}

// La Preview ahora usa el estado directamente, simulando lo que haría el ViewModel.
@Preview(showBackground = true)
@Composable
fun GreetingScreenPreview() {
    ProjecteANDROIDTheme {
        // La preview també necessita que creïs el model correctament.
        GreetingScreen(uiState = GreetingModel(message = "Hello Preview!"))
    }
}