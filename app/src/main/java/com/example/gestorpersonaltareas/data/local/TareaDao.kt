package com.example.gestorpersonaltareas.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {

    /** Emite la lista completa cada vez que haya cambios en la tabla. */
    @Query("SELECT * FROM tareas ORDER BY id ASC")
    fun getAllTareas(): Flow<List<TareaEntity>>

    /** Obtiene una tarea por su id. */
    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun getTareaById(id: Int): TareaEntity?

    /** Inserta una tarea nueva; si ya existiese el id la reemplaza. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaEntity): Long

    /** Actualiza todos los campos de una tarea existente. */
    @Update
    suspend fun updateTarea(tarea: TareaEntity)

    /** Elimina la tarea cuyo id coincida. */
    @Query("DELETE FROM tareas WHERE id = :id")
    suspend fun deleteTareaById(id: Int)
}
