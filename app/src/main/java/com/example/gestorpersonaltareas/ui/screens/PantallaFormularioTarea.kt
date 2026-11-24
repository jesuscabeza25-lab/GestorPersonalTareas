package com.example.gestorpersonaltareas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestorpersonaltareas.model.Prioridad
import com.example.gestorpersonaltareas.model.Tarea
import com.example.gestorpersonaltareas.ui.TareasViewModel

/** Pantalla de creación y edición de tareas */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioTarea(
    viewModel: TareasViewModel,
    tareaAEditar: Tarea?,         // null = modo creación
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
) {
    val esEdicion = tareaAEditar != null

    // Estado del formulario – se conserva en rotaciones
    var titulo by rememberSaveable { mutableStateOf(tareaAEditar?.titulo ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(tareaAEditar?.descripcion ?: "") }
    var prioridad by rememberSaveable { mutableStateOf(tareaAEditar?.prioridad ?: Prioridad.MEDIA) }

    // Validación
    var tituloError by rememberSaveable { mutableStateOf("") }

    // Protección contra doble tap en Guardar
    var guardando by rememberSaveable { mutableStateOf(false) }

    // Estado del dropdown
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar tarea" else "Nueva tarea") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Campo Título ──────────────────────────────────────────────────
            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    tituloError = ""  // limpiar error al escribir
                },
                label = { Text("Título *") },
                isError = tituloError.isNotEmpty(),
                supportingText = {
                    if (tituloError.isNotEmpty()) {
                        Text(
                            text = tituloError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Campo Descripción ─────────────────────────────────────────────
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (opcional)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Selector de Prioridad ─────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = prioridadLabel(prioridad),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Prioridad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    Prioridad.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(prioridadLabel(opcion)) },
                            onClick = {
                                prioridad = opcion
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Botones ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (guardando) return@Button     // evitar doble tap

                        // Validación
                        val tituloTrimmed = titulo.trim()
                        when {
                            tituloTrimmed.isEmpty() -> {
                                tituloError = "El título no puede estar vacío."
                                return@Button
                            }
                            tituloTrimmed.length < 3 -> {
                                tituloError = "El título debe tener al menos 3 caracteres."
                                return@Button
                            }
                        }

                        guardando = true
                        if (esEdicion) {
                            viewModel.editarTarea(
                                id = tareaAEditar!!.id,
                                titulo = tituloTrimmed,
                                descripcion = descripcion,
                                prioridad = prioridad
                            )
                        } else {
                            viewModel.agregarTarea(
                                titulo = tituloTrimmed,
                                descripcion = descripcion,
                                prioridad = prioridad
                            )
                        }
                        onGuardar()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !guardando
                ) {
                    Text("Guardar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun prioridadLabel(prioridad: Prioridad) = when (prioridad) {
    Prioridad.BAJA -> "Baja"
    Prioridad.MEDIA -> "Media"
    Prioridad.ALTA -> "Alta"
}
