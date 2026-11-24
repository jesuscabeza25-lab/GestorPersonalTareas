package com.example.gestorpersonaltareas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gestorpersonaltareas.model.Prioridad
import com.example.gestorpersonaltareas.model.Tarea

@Entity(tableName = "tareas")
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val prioridad: String,       // Almacenamos el name() del enum para evitar índices frágiles
    val completada: Boolean
)

// ── Mappers ────────────────────────────────────────────────────────────────────

fun TareaEntity.toDomain(): Tarea = Tarea(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    prioridad = Prioridad.valueOf(prioridad),
    completada = completada
)

fun Tarea.toEntity(): TareaEntity = TareaEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    prioridad = prioridad.name,
    completada = completada
)
