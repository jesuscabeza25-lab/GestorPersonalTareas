package com.example.gestorpersonaltareas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.gestorpersonaltareas.model.Prioridad
import com.example.gestorpersonaltareas.model.Tarea
import com.example.gestorpersonaltareas.ui.TareasViewModel
import com.example.gestorpersonaltareas.ui.screens.PantallaFormularioTarea
import com.example.gestorpersonaltareas.ui.screens.PantallaListaTareas
import com.example.gestorpersonaltareas.ui.screens.PantallaTareasEjemplo
import com.example.gestorpersonaltareas.ui.theme.GestorPersonalTareasTheme

/**
 * Destinos de navegación interna gestionados mediante estados de Compose.
 */
enum class DestinoNavegacion {
    LISTA,
    NUEVA_TAREA,
    EDITAR_TAREA,
    TAREAS_EJEMPLO
}

class MainActivity : ComponentActivity() {

    private val viewModel: TareasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorPersonalTareasTheme {
                AppNavegacion(
                    viewModel = viewModel,
                    onCompartir = { tarea -> compartirTarea(tarea) }
                )
            }
        }
    }

    /**
     * Comparte el contenido de una tarea personal mediante un Intent implícito (ACTION_SEND).
     * Incluye título, descripción, prioridad y estado (completada/pendiente).
     * Gestiona el caso en que no existan aplicaciones compatibles en el dispositivo.
     */
    private fun compartirTarea(tarea: Tarea) {
        val estadoTexto = if (tarea.completada) "Completada" else "Pendiente"
        val prioridadTexto = when (tarea.prioridad) {
            Prioridad.BAJA -> "Baja"
            Prioridad.MEDIA -> "Media"
            Prioridad.ALTA -> "Alta"
        }

        val texto = buildString {
            append("📋 Tarea: ${tarea.titulo}\n")
            if (tarea.descripcion.isNotBlank()) {
                append("Descripción: ${tarea.descripcion}\n")
            }
            append("Prioridad: $prioridadTexto\n")
            append("Estado: $estadoTexto")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
            putExtra(Intent.EXTRA_SUBJECT, "Tarea: ${tarea.titulo}")
        }

        val chooserIntent = Intent.createChooser(sendIntent, "Compartir tarea con...")

        try {
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No hay aplicaciones disponibles en el dispositivo para compartir.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir el selector de aplicaciones.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/**
 * Gestor de navegación reactivo basado en estados de Compose y rememberSaveable
 * para conservar la navegación ante cambios de configuración (rotación de pantalla).
 */
@Composable
fun AppNavegacion(
    viewModel: TareasViewModel,
    onCompartir: (Tarea) -> Unit,
) {
    var destinoActual by rememberSaveable { mutableStateOf(DestinoNavegacion.LISTA) }
    var tareaAEditarId by rememberSaveable { mutableStateOf<Int?>(null) }

    val tareas by viewModel.tareas.collectAsState()

    when (destinoActual) {
        DestinoNavegacion.LISTA -> {
            PantallaListaTareas(
                viewModel = viewModel,
                onNuevaTarea = { destinoActual = DestinoNavegacion.NUEVA_TAREA },
                onEditarTarea = { tarea ->
                    tareaAEditarId = tarea.id
                    destinoActual = DestinoNavegacion.EDITAR_TAREA
                },
                onVerEjemplos = { destinoActual = DestinoNavegacion.TAREAS_EJEMPLO },
                onCompartir = onCompartir
            )
        }

        DestinoNavegacion.NUEVA_TAREA -> {
            PantallaFormularioTarea(
                viewModel = viewModel,
                tareaAEditar = null,
                onGuardar = { destinoActual = DestinoNavegacion.LISTA },
                onCancelar = { destinoActual = DestinoNavegacion.LISTA }
            )
        }

        DestinoNavegacion.EDITAR_TAREA -> {
            val tareaAEditar = tareas.find { it.id == tareaAEditarId }
            PantallaFormularioTarea(
                viewModel = viewModel,
                tareaAEditar = tareaAEditar,
                onGuardar = {
                    tareaAEditarId = null
                    destinoActual = DestinoNavegacion.LISTA
                },
                onCancelar = {
                    tareaAEditarId = null
                    destinoActual = DestinoNavegacion.LISTA
                }
            )
        }

        DestinoNavegacion.TAREAS_EJEMPLO -> {
            PantallaTareasEjemplo(
                viewModel = viewModel,
                onVolver = { destinoActual = DestinoNavegacion.LISTA }
            )
        }
    }
}
