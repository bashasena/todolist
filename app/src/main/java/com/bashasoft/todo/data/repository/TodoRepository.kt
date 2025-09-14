package com.bashasoft.todo.data.repository

import com.bashasoft.todo.data.local.TodoDao
import com.bashasoft.todo.data.local.TodoEntity
import com.bashasoft.todo.data.mapper.TodoMapper.toCreateRequest
import com.bashasoft.todo.data.mapper.TodoMapper.toDomain
import com.bashasoft.todo.data.mapper.TodoMapper.toEntity
import com.bashasoft.todo.data.model.TodoModel
import com.bashasoft.todo.data.model.TodoResponseModel
import com.bashasoft.todo.data.model.CreateTodoRequest
import com.bashasoft.todo.data.remote.api.TodoApiService
import com.bashasoft.todo.domain.model.Todo
import com.bashasoft.todo.domain.repository.TodoRepository as DomainTodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log
import java.util.UUID
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val apiService: TodoApiService,
    private val todoDao: TodoDao
) : DomainTodoRepository {
    
    fun getMessage(): String = "Hilt is working!"
    
    // Get todos from local database (Flow for reactive updates)
    override fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    // Add todo locally first, then sync to network
    override suspend fun addTodo(todo: Todo): Todo {
        Log.d("TodoRepository", "Adding todo locally: $todo")
        
        // Generate ID if not provided
        val todoWithId = if (todo.id.isEmpty()) {
            todo.copy(id = UUID.randomUUID().toString())
        } else {
            todo
        }
        
        // Save to local database first (marked as unsynced)
        val entity = todoWithId.copy(isSynced = false).toEntity()
        todoDao.insertTodo(entity)
        Log.d("TodoRepository", "Todo saved to local database: $entity")
        
        // Try to sync to network in background
        try {
            syncTodoToNetwork(entity)
        } catch (e: Exception) {
            Log.e("TodoRepository", "Failed to sync todo immediately: ${entity.id}", e)
        }
        
        return todoWithId
    }
    
    // Delete todo locally first, then sync to network
    override suspend fun deleteTodo(id: String) {
        Log.d("TodoRepository", "Deleting todo locally: $id")
        
        // Get the todo to check if it was synced
        val entity = todoDao.getTodoById(id)
        
        // If it was synced, try to delete from network first
        if (entity?.isSynced == true) {
            try {
                apiService.deleteTodo(id)
                Log.d("TodoRepository", "Successfully deleted from network: $id")
            } catch (e: Exception) {
                Log.e("TodoRepository", "Failed to delete from network: $id", e)
                Log.e("TodoRepository", "Error details: ${e.message}")
                
                // If it's a 500 error or todo not found, it might be a UUID that was never synced
                // In this case, we'll still delete locally since the server doesn't have it
                if (e.message?.contains("500") == true || e.message?.contains("404") == true) {
                    Log.d("TodoRepository", "Todo may not exist on server (UUID or sync issue), proceeding with local delete: $id")
                } else {
                    // For other errors, we might want to keep the todo locally for retry
                    // But for now, we'll delete locally to avoid data inconsistency
                    Log.d("TodoRepository", "Network delete failed, but deleting locally to maintain consistency: $id")
                }
            }
        }
        
        // Delete from local database (regardless of network success)
        todoDao.deleteTodoById(id)
        Log.d("TodoRepository", "Deleted todo from local database: $id")
    }
    
    // Update todo locally first, then sync to network
    override suspend fun updateTodo(todo: Todo): Todo {
        Log.d("TodoRepository", "Updating todo locally: ${todo.id}")
        
        // Update in local database first (marked as unsynced)
        val entity = todo.copy(isSynced = false).toEntity()
        todoDao.updateTodo(entity)
        Log.d("TodoRepository", "Todo updated in local database: $entity")
        
        // Try to sync to network in background
        try {
            syncTodoUpdateToNetwork(entity)
        } catch (e: Exception) {
            Log.e("TodoRepository", "Failed to sync todo update immediately: ${entity.id}", e)
        }
        
        return todo
    }
    
    // Sync all unsynced todos to network
    override suspend fun syncAllTodos() {
        Log.d("TodoRepository", "Syncing all unsynced todos")
        val unsyncedTodos = todoDao.getUnsyncedTodos()
        
        for (entity in unsyncedTodos) {
            syncTodoToNetwork(entity)
        }
    }
    
    // Fetch todos from network and update local database
    override suspend fun fetchTodosFromNetwork() {
        try {
            Log.d("TodoRepository", "Fetching todos from network")
            val networkTodos = apiService.getTodos()
            Log.d("TodoRepository", "Received ${networkTodos.size} todos from network: $networkTodos")
            
            // Convert to entities and mark as synced
            val entities = networkTodos.map { todo ->
                todo.toDomain().copy(isSynced = true).toEntity()
            }
            
            // Insert/update in local database
            todoDao.insertTodos(entities)
            Log.d("TodoRepository", "Successfully synced ${entities.size} todos from network to local database")
            
        } catch (e: Exception) {
            Log.e("TodoRepository", "Failed to fetch todos from network", e)
            throw e
        }
    }
    
    // Private method to sync individual todo to network
    private suspend fun syncTodoToNetwork(entity: TodoEntity) {
        var request = ""
        try {
            Log.d("TodoRepository", "Syncing todo to network: ${entity.id}")
            Log.d("TodoRepository", "Todo entity details: $entity")
            
            // Convert entity to domain model, then to API request format
            val domainTodo = entity.toDomain()
            val createRequest = domainTodo.toCreateRequest()
            
            Log.d("TodoRepository", "Sending create request: $createRequest")
            val response = apiService.addTodo(createRequest)
            Log.d("TodoRepository", "API response: $response")
            request ="TodoRepository API response: $response"
            
            // Update the local entity with the server's ID if different
            val serverId = response.data.id
            if (serverId != entity.id) {
                Log.d("TodoRepository", "Server returned different ID: $serverId vs local: ${entity.id}")
                // Update the local database with the server ID
                todoDao.updateTodoId(entity.id, serverId)
                Log.d("TodoRepository", "Updated local todo ID from ${entity.id} to $serverId")
            }
            
            // Mark as synced if successful
            todoDao.markAsSynced(serverId)
            Log.d("TodoRepository", "Successfully synced todo: $serverId")
            
        } catch (e: Exception) {
            Log.e("TodoRepository", "Failed to sync todo to network: ${entity.id}", e)
            Log.e("TodoRepository", "Error details: ${e.message}")
            Log.e("TodoRepository", "Request that failed: $request")
            // Keep as unsynced for retry later
        }
    }
    
    // Private method to sync todo update to network
    private suspend fun syncTodoUpdateToNetwork(entity: TodoEntity) {
        var request = ""
        try {
            Log.d("TodoRepository", "Syncing todo update to network: ${entity.id}")
            Log.d("TodoRepository", "Updated todo entity details: $entity")
            
            // Convert entity to domain model, then to API request format
            val domainTodo = entity.toDomain()
            val updateRequest = domainTodo.toCreateRequest()
            
            Log.d("TodoRepository", "Sending update request: $updateRequest")
            
            // For updates, we'll use the same endpoint as create since the API doesn't have a specific update endpoint
            // The API will handle this based on whether the todo already exists
            val response = apiService.addTodo(updateRequest)
            Log.d("TodoRepository", "API update response: $response")
            request = "TodoRepository API update response: $response"
            
            // Mark as synced if successful
            todoDao.markAsSynced(entity.id)
            Log.d("TodoRepository", "Successfully synced todo update: ${entity.id}")
            
        } catch (e: Exception) {
            Log.e("TodoRepository", "Failed to sync todo update to network: ${entity.id}", e)
            Log.e("TodoRepository", "Error details: ${e.message}")
            Log.e("TodoRepository", "Request that failed: $request")
            // Keep as unsynced for retry later
        }
    }
    
    // Get unsynced todos count for UI indication
    override suspend fun getUnsyncedCount(): Int {
        return todoDao.getUnsyncedTodos().size
    }
    
    // Get unsynced todos for debugging
    override suspend fun getUnsyncedTodos(): List<Todo> {
        return todoDao.getUnsyncedTodos().map { it.toDomain() }
    }
}