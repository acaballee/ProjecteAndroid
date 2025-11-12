package com.example.projecteandroid.ui.theme


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecteandroid.data.WelcomeState

// View: La pantalla que muestra los datos del ViewModel.
// --- VISTA PRINCIPAL (LA PANTALLA) ---
@Composable
fun WelcomeScreen(
    uiState: WelcomeState,
    onUsernameChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onShowCreatorDialog: () -> Unit,
    modifier: Modifier = Modifier,
    onPasswordChange: (String) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Títol de l'App
            Text(
                text = uiState.appTitle,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Descripció de l'App
            Text(
                text = uiState.appDescription,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Camp de text per a l'usuari
            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text("Nom d'usuari") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Camp de text per a la contrasenya
            OutlinedTextField(
                value = uiState.password, // <-- CANVI
                onValueChange = onPasswordChange, // <-- CANVI
                label = { Text("Contrasenya") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), // Amaga el text
                modifier = Modifier.fillMaxWidth()
            )
            // Mostra un missatge d'error si n'hi ha
            if (uiState.loginError != null) {
                Text(
                    text = uiState.loginError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botó de Login
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("INICIAR SESSIÓ")
            }
        }

        // Botó d'informació del creador (a la part inferior)
        IconButton(
            onClick = onShowCreatorDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Informació del creador",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- COMPONENT DEL DIÀLEG DEL CREADOR ---
@Composable
fun CreatorInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sobre el Creador") },
        text = {
            Text(
                "Aquesta aplicació ha estat creada per:\n\n" +
                        "Pau Codorniu i Alex Caballe\n" +
                        "alumnes de 2n DAM de l'Ies del Ebre"
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("TANCAR")
            }
        }
    )
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    ProjecteANDROIDTheme {
        val sampleState = WelcomeState(username = "usuari_prova")
        WelcomeScreen(
            uiState = sampleState,
            onUsernameChange = {},
            onLoginClick = {},
            onShowCreatorDialog = {},
            onPasswordChange = {}
        )
    }
}