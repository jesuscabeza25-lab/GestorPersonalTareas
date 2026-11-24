package com.example.gestorpersonaltareas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorpersonaltareas.data.local.AppDatabase
import com.example.gestorpersonaltareas.data.local.TareaEntity
import com.example.gestorpersonaltareas.data.local.toDomain
import com.example.gestorpersonaltareas.data.preferences.UserPreferencesRepository
import com.example.gestorpersonaltareas.data.remote.RetrofitClient
import com.example.gestorpersonaltareas.data.remote.TodoDto
import com.example.gestorpersonaltareas.model.Prioridad
import com.example.gestorpersonaltareas.model.Tarea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Estados de la petición a la API REST (JSONPlaceholder)
 */
sealed interface RemoteTasksUiState {
    object Idle : RemoteTasksUiState
    object Loading : RemoteTasksUiState
    data class Success(val tasks: List<TodoDto>) : RemoteTasksUiState
    data class Error(val message: String) : RemoteTasksUiState
}

class TareasViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).tareaDao()
    private val preferencesRepository = UserPreferencesRepository(application)

    // ── Estado de tareas de la API REST ───────────────────────────────────────
    private val _remoteTasksUiState = MutableStateFlow<RemoteTasksUiState>(RemoteTasksUiState.Idle)
    val remoteTasksUiState: StateFlow<RemoteTasksUiState> = _remoteTasksUiState.asStateFlow()

    /**
     * Preferencia actual observada desde DataStore.
     */
    val mostrarSoloPendientes: StateFlow<Boolean> = preferencesRepository
        .mostrarSoloPendientesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /**
     * Número total de tareas guardadas en Room (sin filtrar).
     * Permite a la UI saber si no hay ninguna tarea en BD o si el filtro las ha ocultado todas.
     */
    val totalTareasCount: StateFlow<Int> = dao
        .getAllTareas()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Lista de tareas combinada con la preferencia de DataStore.
     * Si mostrarSoloPendientes es true, filtra únicamente las tareas con completada == false.
     * Si es false, muestra todas las tareas registradas.
     */
    val tareas: StateFlow<List<Tarea>> = combine(
        dao.getAllTareas().map { entities -> entities.map { it.toDomain() } },
        preferencesRepository.mostrarSoloPendientesFlow
    ) { lista, soloPendientes ->
        if (soloPendientes) {
            lista.filter { !it.completada }
        } else {
            lista
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun setMostrarSoloPendientes(mostrar: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMostrarSoloPendientes(mostrar)
        }
    }

    // ── Operaciones CRUD (Room) ───────────────────────────────────────────────

    fun agregarTarea(titulo: String, descripcion: String, prioridad: Prioridad) {
        viewModelScope.launch {
            dao.insertTarea(
                TareaEntity(
                    titulo = titulo.trim(),
                    descripcion = descripcion.trim(),
                    prioridad = prioridad.name,
                    completada = false
                )
            )
        }
    }

    fun editarTarea(id: Int, titulo: String, descripcion: String, prioridad: Prioridad) {
        viewModelScope.launch {
            val actual = dao.getTareaById(id)
            dao.updateTarea(
                TareaEntity(
                    id = id,
                    titulo = titulo.trim(),
                    descripcion = descripcion.trim(),
                    prioridad = prioridad.name,
                    completada = actual?.completada ?: false
                )
            )
        }
    }

    fun eliminarTarea(id: Int) {
        viewModelScope.launch {
            dao.deleteTareaById(id)
        }
    }

    fun toggleCompletada(id: Int) {
        viewModelScope.launch {
            val actual = dao.getTareaById(id) ?: return@launch
            dao.updateTarea(
                actual.copy(completada = !actual.completada)
            )
        }
    }

    // ── Petición a la API REST (JSONPlaceholder) ──────────────────────────────

    fun cargarTareasRemotas() {
        if (_remoteTasksUiState.value is RemoteTasksUiState.Loading) return
        viewModelScope.launch {
            _remoteTasksUiState.value = RemoteTasksUiState.Loading
            try {
                val respuesta = RetrofitClient.apiService.getTodos(userId = 1)
                _remoteTasksUiState.value = RemoteTasksUiState.Success(respuesta)
            } catch (e: IOException) {
                _remoteTasksUiState.value = RemoteTasksUiState.Error(
                    "Error de conexión. Comprueba que dispones de conexión a Internet o desactiva el modo avión."
                )
            } catch (e: HttpException) {
                _remoteTasksUiState.value = RemoteTasksUiState.Error(
                    "Error al comunicarse con el servidor (HTTP ${e.code()}). Inténtalo de nuevo."
                )
            } catch (e: Exception) {
                _remoteTasksUiState.value = RemoteTasksUiState.Error(
                    "Ha ocurrido un error inesperado: ${e.localizedMessage ?: "error desconocido"}"
                )
            }
        }
    }

    /**
     * Permite importar una tarea de ejemplo a la base de datos local Room.
     */
    fun importarTareaRemota(todo: TodoDto) {
        agregarTarea(
            titulo = todo.title,
            descripcion = "Importada desde la API JSONPlaceholder (ID remoto #${todo.id})",
            prioridad = Prioridad.MEDIA
        )
    }
}
