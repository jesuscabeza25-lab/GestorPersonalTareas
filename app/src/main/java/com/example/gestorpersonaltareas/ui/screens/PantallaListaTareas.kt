package com.example.gestorpersonaltareas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gestorpersonaltareas.model.Prioridad
import com.example.gestorpersonaltareas.model.Tarea
import com.example.gestorpersonaltareas.ui.TareasViewModel

/** Pantalla de listado principal de tareas con filtro persistente */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaTareas(
    viewModel: TareasViewModel,
    onNuevaTarea: () -> Unit,
    onEditarTarea: (Tarea) -> Unit,
    onVerEjemplos: () -> Unit,
    onCompartir: (Tarea) -> Unit,
) {
    // Recoge la lista filtrada de tareas y la preferencia de DataStore
    val tareas by viewModel.tareas.collectAsState()
    val mostrarSoloPendientes by viewModel.mostrarSoloPendientes.collectAsState()
    val totalTareasCount by viewModel.totalTareasCount.collectAsState()

    // ID de la tarea pendiente de eliminar (null = ninguna)
    var tareaAEliminar by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Tareas") },
                actions = {
                    TextButton(onClick = onVerEjemplos) {
                        Text("Ejemplos")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaTarea) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Control Switch para "Mostrar solo pendientes" (persistido en DataStore)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mostrar solo pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = mostrarSoloPendientes,
                    onCheckedChange = { viewModel.setMostrarSoloPendientes(it) }
                )
            }

            HorizontalDivider()

            // Visualización de la lista o estados vacíos
            if (tareas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (mostrarSoloPendientes && totalTareasCount > 0) {
                            Text(
                                text = "No hay tareas pendientes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Todas las tareas registradas están completadas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "No tienes tareas todavía",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pulsa + para crear tu primera tarea",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tareas, key = { it.id }) { tarea ->
                        TareaCard(
                            tarea = tarea,
                            onToggleCompletada = { viewModel.toggleCompletada(tarea.id) },
                            onEditar = { onEditarTarea(tarea) },
                            onEliminar = { tareaAEliminar = tarea.id },
                            onCompartir = { onCompartir(tarea) }
                        )
                    }
                }
            }
        }

        // AlertDialog de confirmación de borrado
        if (tareaAEliminar != null) {
            AlertDialog(
                onDismissRequest = { tareaAEliminar = null },
                title = { Text("Eliminar tarea") },
                text = { Text("¿Estás seguro de que quieres eliminar esta tarea? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tareaAEliminar?.let { viewModel.eliminarTarea(it) }
                            tareaAEliminar = null
                        }
                    ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { tareaAEliminar = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

/** Tarjeta individual de tarea */
@Composable
private fun TareaCard(
    tarea: Tarea,
    onToggleCompletada: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCompartir: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = { onToggleCompletada() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    PrioridadChip(prioridad = tarea.prioridad)
                }
            }

            if (tarea.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Fila de acciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Compartir tarea mediante Intent implícito
                IconButton(onClick = onCompartir) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir tarea",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar tarea",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar tarea",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Chip de color según prioridad */
@Composable
private fun PrioridadChip(prioridad: Prioridad) {
    val (label, color) = when (prioridad) {
        Prioridad.BAJA -> "Baja" to MaterialTheme.colorScheme.tertiary
        Prioridad.MEDIA -> "Media" to MaterialTheme.colorScheme.secondary
        Prioridad.ALTA -> "Alta" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
