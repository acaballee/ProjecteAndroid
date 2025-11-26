package com.example.projecteandroid.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projecteandroid.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    onAddTask: (title: String, subject: String, dueDate: String) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onNavigateBack: () -> Unit, // Paràmetre per a l'acció de tornar enrere
    ) {
    var showDialog by remember { mutableStateOf(false) }

    // Scaffold ens proporciona una estructura amb barra superior, contingut, etc.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Les Meves Tasques") },
                navigationIcon = {
                    // La icona de la fletxa que executarà l'acció de tornar
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tornar enrere"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        // Com que ja tenies una LazyColumn, la posem dins del Scaffold
        // i apliquem el padding corresponent.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Important aplicar el padding per no quedar sota la barra
        ) {
            items(tasks) { task ->
                // Aquí pots posar el teu Composable per a cada item de la tasca.
                // De moment, deixo un Text simple.
                Text(text = "Tasca: ${task.title}", modifier = Modifier.padding(all = 16.dp))
            }
        }
    }

        if (showDialog) {
            AddTaskDialog(
                onDismiss = { showDialog = false },
                onConfirm = { title, subject, dueDate ->
                    onAddTask(title, subject, dueDate)
                    showDialog = false
                }
            )
        }
    }

@Composable
fun TaskItem(task: Task, onToggle: (Task) -> Unit, onDelete: (Task) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle(task) })
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text("Assignatura: ${task.subject}")
                Text("Data límit: ${task.dueDate}")
            }
            IconButton(onClick = { onDelete(task) }) {
                Text("X", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (title: String, subject: String, dueDate: String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Tasca") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Títol") })
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Assignatura") })
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Data límit (YYYY-MM-DD)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, subject, dueDate) }) {
                Text("Desar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}
