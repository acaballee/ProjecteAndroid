package com.example.projecteandroid.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.projecteandroid.data.WelcomeState

@Composable
fun WelcomeScreen(
    uiState: WelcomeState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit, // Funció per a la contrasenya
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit, // per al botó de registre
    onShowCreatorDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Nou: TextField per a l'usuari i contrasenya
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    // Aquesta variable controlarà la visibilitat del menú desplegable
    var creatorMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.appTitle,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.appDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text("Nom d'usuari") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Contrasenya") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onLoginClick()
                }),
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.loginError != null) {
                Text(
                    text = uiState.loginError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Inicia sessió")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // NOU: Botó per registrar-se
            TextButton(onClick = onRegisterClick) {
                Text("No tens compte? Registra't")
            }
        }

        // NOU: Icona a la part inferior dreta amb el menú desplegable
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            IconButton(onClick = { creatorMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Sobre el creador"
                )
            }

            DropdownMenu(
                expanded = creatorMenuExpanded,
                onDismissRequest = { creatorMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Creat per Àlex i Pau") },
                    onClick = { creatorMenuExpanded = false }
                )
            }
        }
    }
}
