package com.example.gestorpersonaltareas.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface JsonPlaceholderApi {
    @GET("todos")
    suspend fun getTodos(@Query("userId") userId: Int = 1): List<TodoDto>
}
