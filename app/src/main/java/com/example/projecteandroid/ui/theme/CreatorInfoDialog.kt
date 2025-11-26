package com.example.projecteandroid.ui.theme

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun CreatorInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Sobre el creador") },
        text = { Text(text = "Aquesta aplicació ha estat creada per Àlex i Pau.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tancar")
            }
        }
    )
}