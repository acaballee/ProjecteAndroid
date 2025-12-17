package com.example.projecteandroid.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projecteandroid.data.Task
import com.example.projecteandroid.data.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.positionInRoot
import com.example.projecteandroid.data.TaskPriority
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.absoluteValue



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    username: String,
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit,
    onAddTask: (title: String, subject: String, dueDate: String, priority: TaskPriority) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onMoveTask: (Task, TaskStatus) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onLogout: () -> Unit,
) {
    // Estat per controlar la visibilitat del diàleg i la tasca que s'està editant/creant
    var showDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tauler de Tasques") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuració"
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

        // Barra de progrés
        val totalTasks = tasks.size
        val completedCount = completedTasks.size
        val progress = if (totalTasks > 0) completedCount.toFloat() / totalTasks else 0f

        Column(modifier = Modifier.padding(innerPadding)) {
            if (totalTasks > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Text(
                    text = "${(progress * 100).toInt()}% Completat",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp, bottom = 8.dp)
                )
            }

            KanbanBoard(
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
    }

    if (showDialog) {
        TaskDialog(
            task = taskToEdit,
            onDismiss = { showDialog = false },
            onConfirm = { title, subject, dueDate, priority ->
                if (taskToEdit == null) {
                    onAddTask(title, subject, dueDate, priority)
                } else {
                    onUpdateTask(taskToEdit!!.copy(title = title, subject = subject, dueDate = dueDate, priority = priority))
                }
                showDialog = false
            }
        )
    }
    // Parametres del diàleg de ajustes
    if (showSettingsDialog) {
        SettingsDialog(
            username = username,
            isDarkTheme = isDarkTheme,
            onThemeChange = onThemeChange,
            onLogout = onLogout,
            onDismiss = { showSettingsDialog = false }
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
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var boardPosition by remember { mutableStateOf(Offset.Zero) }

    // MAPA per guardar on està cada columna a la pantalla
    val columnAreas = remember { mutableStateMapOf<TaskStatus, Rect>() }

    Box(modifier = modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            boardPosition = coordinates.boundsInWindow().topLeft
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // NOU: Ara acceptem la 'startPosition' (coordenades globals de la targeta)
            val onDragStartAction: (Task, Offset) -> Unit = { task, startPosition ->
                draggedTask = task
                // NOU: En lloc de començar a 0, comencem on està la targeta realment
                dragOffset = startPosition - boardPosition
            }

            val onDragAction: (Offset) -> Unit = { delta ->
                dragOffset += delta
            }

            val onDragEndAction: () -> Unit = {
                draggedTask?.let { task ->
                    val startRegion = columnAreas[task.status]
                    if (startRegion != null) {
                        // NOU: El càlcul ara és més directe perquè dragOffset ja té la posició real
                        // El centre de la fitxa arrossegada és dragOffset + meitat de l'amplada (aprox)
                        // Per simplificar, mirem la cantonada superior esquerra del drop
                        val dropX = dragOffset.x + boardPosition.x + 50 // Sumem una mica per centrar la detecció

                        val targetStatus = columnAreas.entries.firstOrNull { (_, rect) ->
                            dropX >= rect.left && dropX <= rect.right
                        }?.key

                        if (targetStatus != null && targetStatus != task.status) {
                            onMoveTask(task, targetStatus)
                        }
                    }
                }
                draggedTask = null
                dragOffset = Offset.Zero
            }

            // Columnes
            TaskColumn(
                status = TaskStatus.PENDING,
                tasks = pendingTasks,
                modifier = Modifier.weight(1f),
                onMoveTask = onMoveTask,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onDragStart = onDragStartAction,
                onDrag = onDragAction,
                onDragEnd = onDragEndAction,
                onColumnPositioned = { rect -> columnAreas[TaskStatus.PENDING] = rect }
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
                onDragEnd = onDragEndAction,
                onColumnPositioned = { rect -> columnAreas[TaskStatus.IN_PROGRESS] = rect }
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
                onDragEnd = onDragEndAction,
                onColumnPositioned = { rect -> columnAreas[TaskStatus.COMPLETED] = rect }
            )
        }

        // VISUALITZACIÓ DE L'ARROSSEGAMENT
        draggedTask?.let { task ->
            Box(modifier = Modifier.offset {
                androidx.compose.ui.unit.IntOffset(dragOffset.x.toInt(), dragOffset.y.toInt())
            }) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth(0.33f) // Mantenim la mida reduïda que volies
                ) {
                    TaskCard(
                        task = task,
                        onEditClick = {},
                        onDeleteClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onColumnPositioned: (Rect) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .onGloballyPositioned { coordinates ->
                onColumnPositioned(coordinates.boundsInWindow())
            }
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ -> change.consume() },
                    onDragEnd = { }
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
                // NOU: Variable per guardar la posició d'AQUESTA targeta concreta
                var itemPosition by remember { mutableStateOf(Offset.Zero) }

                TaskCard(
                    task = task,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    modifier = Modifier
                        // NOU: Detectem on està la targeta a la pantalla
                        .onGloballyPositioned { coordinates ->
                            itemPosition = coordinates.positionInRoot()
                        }
                        .pointerInput(task) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    // NOU: Passem la posició global (itemPosition) en lloc de l'offset local
                                    onDragStart(task, itemPosition)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                // Indicador de prioritat
                val priorityColor = when (task.priority) {
                    TaskPriority.HIGH -> Color.Red
                    TaskPriority.MEDIUM -> Color.Yellow
                    TaskPriority.LOW -> Color.Green
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(priorityColor)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            // Assignatura amb color
            Surface(
                color = getSubjectColor(task.subject),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = task.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
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
    onConfirm: (title: String, subject: String, dueDate: String, priority: TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var subject by remember { mutableStateOf(task?.subject ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }

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
                Box {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { }, // No permetem escriure manualment
                        label = { Text("Data límit (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true, // Fem que sigui de només lectura
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
                    // Superposició transparent per detectar clics a tot el camp
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Prioritat
                Text("Prioritat", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TaskPriority.values().forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name) },
                            leadingIcon = if (priority == p) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }

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
                        onClick = { onConfirm(title, subject, dueDate, priority) },
                        enabled = isTitleValid
                    ) {
                        Text("Desar")
                    }
                }
            }
        }
    }
}

// Funció per generar un color consistent a partir del nom de l'assignatura
fun getSubjectColor(subject: String): Color {
    val hash = subject.hashCode()
    val hue = (hash % 360).absoluteValue.toFloat()
    val saturation = 0.6f // Colors pastís
    val lightness = 0.8f
    return Color(ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness)))
}

// Funció auxiliar per formatar la data
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
fun SettingsDialog(
    username: String,
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                // Perfil
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perfil:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Tema
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mode Fosc",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeChange() }
                    )
                }

                Divider()

                // Logout
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tancar Sessió")
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Tancar")
                }
            }
        }
    }
}
