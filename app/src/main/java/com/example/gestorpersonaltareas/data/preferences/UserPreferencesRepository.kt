package com.example.gestorpersonaltareas.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val MOSTRAR_SOLO_PENDIENTES = booleanPreferencesKey("mostrar_solo_pendientes")
    }

    /**
     * Flow que expone la preferencia "Mostrar solo pendientes".
     * Valor por defecto: false (muestra todas las tareas).
     */
    val mostrarSoloPendientesFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.MOSTRAR_SOLO_PENDIENTES] ?: false
        }

    /**
     * Guarda la preferencia en el DataStore de forma asíncrona.
     */
    suspend fun setMostrarSoloPendientes(mostrarSoloPendientes: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOSTRAR_SOLO_PENDIENTES] = mostrarSoloPendientes
        }
    }
}
