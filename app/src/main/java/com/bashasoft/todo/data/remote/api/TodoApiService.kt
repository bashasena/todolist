package com.bashasoft.todo.data.remote.api

import com.bashasoft.todo.data.model.TodoModel
import com.bashasoft.todo.data.model.TodoResponseModel
import com.bashasoft.todo.data.model.CreateTodoRequest
import com.bashasoft.todo.data.model.CreateTodoResponseModel
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TodoApiService {

    @GET("fake-api/todos")
    suspend fun getTodos(): List<TodoModel>

    @POST("fake-api/todos")
    suspend fun addTodo(@Body todo: CreateTodoRequest): CreateTodoResponseModel

    @DELETE("fake-api/todos/{id}")
    suspend fun deleteTodo(@Path("id") id: String): TodoResponseModel
}