package com.example.projecteandroid.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projecteandroid.data.Task
import com.example.projecteandroid.data.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    onAddTask: (title: String, subject: String, dueDate: String) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onMoveTask: (Task, TaskStatus) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onNavigateBack: () -> Unit,
) {
    // Estat per controlar la visibilitat del diàleg i la tasca que s'està editant/creant
    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tauler de Tasques") },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Tancar sessió"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                taskToEdit = null // Assegurem que és per crear una nova tasca
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Afegir Tasca")
            }
        }
    ) { innerPadding ->
        // Agrupem les tasques per estat
        val tasksByStatus = tasks.groupBy { it.status }
        val pendingTasks = tasksByStatus[TaskStatus.PENDING] ?: emptyList()
        val inProgressTasks = tasksByStatus[TaskStatus.IN_PROGRESS] ?: emptyList()
        val completedTasks = tasksByStatus[TaskStatus.COMPLETED] ?: emptyList()

        KanbanBoard(
            modifier = Modifier.padding(innerPadding),
            pendingTasks = pendingTasks,
            inProgressTasks = inProgressTasks,
            completedTasks = completedTasks,
            onMoveTask = onMoveTask,
            onEditClick = { task ->
                taskToEdit = task
                showDialog = true
            },
            onDeleteClick = onDeleteTask
        )
    }

    if (showDialog) {
        TaskDialog(
            task = taskToEdit,
            onDismiss = { showDialog = false },
            onConfirm = { title, subject, dueDate ->
                if (taskToEdit == null) {
                    onAddTask(title, subject, dueDate)
                } else {
                    onUpdateTask(taskToEdit!!.copy(title = title, subject = subject, dueDate = dueDate))
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun KanbanBoard(
    modifier: Modifier = Modifier,
    pendingTasks: List<Task>,
    inProgressTasks: List<Task>,
    completedTasks: List<Task>,
    onMoveTask: (Task, TaskStatus) -> Unit,
    onEditClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit
) {
    var draggedTask by remember { mutableStateOf<Task?>(null) }
    // Guardem l'offset en píxels (Offset)
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Definim una funció comuna per quan s'arrossega
            val onDragStartAction: (Task, Offset) -> Unit = { task, offset ->
                draggedTask = task
                dragOffset = offset
            }

            // Definim una funció per actualitzar la posició mentre movem el dit
            val onDragAction: (Offset) -> Unit = { delta ->
                dragOffset += delta
            }

            // Definim què passa quan soltem (aquí hauries de calcular on cau)
            val onDragEndAction: () -> Unit = {
                draggedTask = null
                // Aquí és on hauries de comprovar coordenades per fer el onMoveTask realment
            }

            // Columnes del tauler Kanban
            TaskColumn(
                status = TaskStatus.PENDING,
                tasks = pendingTasks,
                modifier = Modifier.weight(1f),
                onMoveTask = onMoveTask,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onDragStart = onDragStartAction,
                onDrag = onDragAction,
                onDragEnd = onDragEndAction
            )
            TaskColumn(
                status = TaskStatus.IN_PROGRESS,
                tasks = inProgressTasks,
                modifier = Modifier.weight(1f),
                onMoveTask = onMoveTask,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onDragStart = onDragStartAction,
                onDrag = onDragAction,
                onDragEnd = onDragEndAction
            )
            TaskColumn(
                status = TaskStatus.COMPLETED,
                tasks = completedTasks,
                modifier = Modifier.weight(1f),
                onMoveTask = onMoveTask,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onDragStart = onDragStartAction,
                onDrag = onDragAction,
                onDragEnd = onDragEndAction
            )
        }

        // Dibuixem la tasca que s'està arrossegant
        draggedTask?.let { task ->
            // Utilitzem un modificador especial per moure elements amb Offset en píxels
            Box(modifier = Modifier.offset {
                androidx.compose.ui.unit.IntOffset(dragOffset.x.toInt(), dragOffset.y.toInt())
            }) {
                TaskCard(task = task, onEditClick = {}, onDeleteClick = {})
            }
        }
    }
}

@Composable
fun TaskColumn(
    status: TaskStatus,
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    onMoveTask: (Task, TaskStatus) -> Unit,
    onEditClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onDragStart: (Task, Offset) -> Unit,
    onDrag: (Offset) -> Unit,       // <--- NOU: Necessari per actualitzar posició
    onDragEnd: () -> Unit           // <--- NOU: Necessari per saber quan soltem
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
            // Error 1 arreglat: Afegim onDrag (encara que estigui buit aquí, és obligatori)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ -> change.consume() },
                    onDragEnd = {
                        // Lògica per detectar drop a la columna (complexa, la deixem buida de moment)
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    modifier = Modifier.pointerInput(task) {
                        // Error 2 arreglat: Implementem onDrag i passem les dades amunt
                        detectDragGestures(
                            onDragStart = { offset ->
                                onDragStart(task, offset)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount) // Passem el moviment al pare
                            },
                            onDragEnd = {
                                onDragEnd()
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEditClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(task.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(task.subject, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Límit: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.align(Alignment.End)) {
                IconButton(onClick = { onEditClick(task) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { onDeleteClick(task) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Esborrar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var subject by remember { mutableStateOf(task?.subject ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: "") }

    // Estats per controlar el calendari
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val isTitleValid by remember { derivedStateOf { title.isNotBlank() } }

    // --- DIÀLEG DEL CALENDARI ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convertim els mil·lisegons a String YYYY-MM-DD
                        dueDate = convertMillisToDate(millis)
                    }
                    showDatePicker = false
                }) {
                    Text("D'acord")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel·lar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- DIÀLEG PRINCIPAL DE LA TASCA ---
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (task == null) "Nova Tasca" else "Editar Tasca",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Camp Títol
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Títol") },
                    isError = !isTitleValid,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Camp Assignatura
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Assignatura") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Camp Data amb botó de calendari
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Data límit (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    // Afegim la icona a la dreta (trailingIcon)
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar data"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botons d'acció
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel·lar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(title, subject, dueDate) },
                        enabled = isTitleValid
                    ) {
                        Text("Desar")
                    }
                }
            }
        }
    }
}

// Funció auxiliar per formatar la data
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}
