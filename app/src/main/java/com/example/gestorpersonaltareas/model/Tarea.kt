package com.example.gestorpersonaltareas.model

enum class Prioridad {
    BAJA,
    MEDIA,
    ALTA
}

data class Tarea(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val completada: Boolean
)
