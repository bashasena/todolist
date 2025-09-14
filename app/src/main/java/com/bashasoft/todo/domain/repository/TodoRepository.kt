package com.bashasoft.todo.domain.repository

import com.bashasoft.todo.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    
    /**
     * Get all todos as a Flow for reactive updates
     */
    fun getAllTodos(): Flow<List<Todo>>
    
    /**
     * Add a new todo locally and sync to network
     */
    suspend fun addTodo(todo: Todo): Todo
    
    /**
     * Delete a todo locally and sync deletion to network
     */
    suspend fun deleteTodo(id: String)
    
    /**
     * Update a todo locally and sync to network
     */
    suspend fun updateTodo(todo: Todo): Todo
    
    /**
     * Sync all unsynced todos to network
     */
    suspend fun syncAllTodos()
    
    /**
     * Fetch todos from network and update local database
     */
    suspend fun fetchTodosFromNetwork()
    
    /**
     * Get count of unsynced todos
     */
    suspend fun getUnsyncedCount(): Int
    
    /**
     * Get list of unsynced todos for debugging
     */
    suspend fun getUnsyncedTodos(): List<Todo>
}
