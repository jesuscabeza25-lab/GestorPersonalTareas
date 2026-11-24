package com.example.gestorpersonaltareas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.gestorpersonaltareas.data.remote.TodoDto
import com.example.gestorpersonaltareas.ui.RemoteTasksUiState
import com.example.gestorpersonaltareas.ui.TareasViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla para consultar tareas de ejemplo mediante la API REST pública JSONPlaceholder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareasEjemplo(
    viewModel: TareasViewModel,
    onVolver: () -> Unit,
) {
    val uiState by viewModel.remoteTasksUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val estaCargando = uiState is RemoteTasksUiState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tareas de ejemplo (API)") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.cargarTareasRemotas() },
                        enabled = !estaCargando
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar tareas")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is RemoteTasksUiState.Idle -> {
                    EstadoInicial(
                        onCargar = { viewModel.cargarTareasRemotas() },
                        habilitado = !estaCargando
                    )
                }

                is RemoteTasksUiState.Loading -> {
                    EstadoCargando()
                }

                is RemoteTasksUiState.Error -> {
                    EstadoError(
                        mensaje = state.message,
                        onReintentar = { viewModel.cargarTareasRemotas() }
                    )
                }

                is RemoteTasksUiState.Success -> {
                    EstadoExito(
                        tasks = state.tasks,
                        onImportar = { todo ->
                            viewModel.importarTareaRemota(todo)
                            scope.launch {
                                snackbarHostState.showSnackbar("Tarea importada a tus tareas personales")
                            }
                        }
                    )
                }
            }
        }
    }
}

/** Estado inicial (Idle): invita al usuario a pulsar el botón para solicitar las tareas. */
@Composable
private fun EstadoInicial(
    onCargar: () -> Unit,
    habilitado: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Tareas de ejemplo externas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Pulsa el botón para solicitar las tareas al servidor mediante la API REST (JSONPlaceholder).",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onCargar,
                enabled = habilitado
            ) {
                Text("Solicitar tareas al servidor")
            }
        }
    }
}

/** Estado de carga (Loading): muestra un CircularProgressIndicator. */
@Composable
private fun EstadoCargando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Consultando el servicio REST...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Estado de error: muestra el mensaje informativo y botón de reintento. */
@Composable
private fun EstadoError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Error de comunicación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onReintentar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reintentar")
            }
        }
    }
}

/** Estado de éxito: muestra la lista desplazable de tareas obtenidas de la API. */
@Composable
private fun EstadoExito(
    tasks: List<TodoDto>,
    onImportar: (TodoDto) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se recibieron tareas del servidor",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🌐 Datos recibidos de JSONPlaceholder",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Estas tareas son datos de ejemplo y no modifican tu base de datos Room salvo que las importes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            items(tasks, key = { it.id }) { todo ->
                TodoItemCard(todo = todo, onImportar = { onImportar(todo) })
            }
        }
    }
}

/** Tarjeta individual para mostrar cada tarea recibida del servicio REST */
@Composable
private fun TodoItemCard(
    todo: TodoDto,
    onImportar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Identificador único recibido del servidor
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "ID: #${todo.id}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Estado de la tarea en el servidor
                val (estadoTexto, estadoColor) = if (todo.completed) {
                    "Finalizada" to MaterialTheme.colorScheme.tertiary
                } else {
                    "Pendiente" to MaterialTheme.colorScheme.secondary
                }
                Surface(
                    color = estadoColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = estadoTexto,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = estadoColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título de la tarea
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de acciones: Importar a tareas personales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onImportar) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Importar a mis tareas",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Importar")
                }
            }
        }
    }
}
