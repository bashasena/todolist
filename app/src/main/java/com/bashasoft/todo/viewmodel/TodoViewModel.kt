package com.bashasoft.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashasoft.todo.domain.model.Todo
import com.bashasoft.todo.domain.model.Priority
import com.bashasoft.todo.domain.model.Status
import com.bashasoft.todo.domain.usecase.AddTodoUseCase
import com.bashasoft.todo.domain.usecase.DeleteTodoUseCase
import com.bashasoft.todo.domain.usecase.GetAllTodosUseCase
import com.bashasoft.todo.domain.usecase.RefreshTodosUseCase
import com.bashasoft.todo.domain.usecase.SyncTodosUseCase
import com.bashasoft.todo.domain.usecase.UpdateTodoUseCase
import com.bashasoft.todo.ui.TodoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val getAllTodosUseCase: GetAllTodosUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val refreshTodosUseCase: RefreshTodosUseCase,
    private val syncTodosUseCase: SyncTodosUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        loadTodos()
    }

    // Load todos from local database (reactive)
    private fun loadTodos() {
        viewModelScope.launch {
            // First, try to sync from network to populate local database
            syncFromNetwork()
            
            // Then observe local database changes
            getAllTodosUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load todos"
                    )
                }
                .collect { todos ->
                    android.util.Log.d("TodoViewModel", "Received ${todos.size} todos from local database: $todos")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        todos = todos.map { it.toUiModel() },
                        error = null
                    )
                }
        }
    }

    // Sync todos from network in background
    private suspend fun syncFromNetwork() {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true)
            refreshTodosUseCase()
            android.util.Log.d("TodoViewModel", "Successfully synced todos from network")
        } catch (e: Exception) {
            // Network sync failure doesn't affect UI
            // Todos are still available from local database
            android.util.Log.e("TodoViewModel", "Network sync failed", e)
            _uiState.value = _uiState.value.copy(
                error = "Failed to sync from network: ${e.message}"
            )
        }
    }

    fun addTodo(todo: com.bashasoft.todo.data.model.TodoModel) {
        viewModelScope.launch {
            try {
                // Convert UI model to domain model
                val domainTodo = todo.toDomain()
                // Add to local database first (immediate UI update)
                addTodoUseCase(domainTodo)
                // No need to refresh - Flow will automatically update UI
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add todo"
                )
            }
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            try {
                // Delete from local database first (immediate UI update)
                deleteTodoUseCase(id)
                // No need to refresh - Flow will automatically update UI
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete todo"
                )
            }
        }
    }

    fun updateTodo(todo: com.bashasoft.todo.data.model.TodoModel) {
        viewModelScope.launch {
            try {
                // Convert UI model to domain model
                val domainTodo = todo.toDomain()
                // Update in local database first (immediate UI update)
                updateTodoUseCase(domainTodo)
                // No need to refresh - Flow will automatically update UI
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update todo"
                )
            }
        }
    }

    fun updateTodoStatus(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                // Find the current todo in the UI state
                val currentTodo = _uiState.value.todos.find { it.id == id }
                if (currentTodo != null) {
                    // Determine new status based on checkbox state
                    val newStatus = if (isCompleted) {
                        Status.COMPLETED
                    } else {
                        Status.NOT_STARTED
                    }
                    
                    // Create updated todo with new status
                    val updatedTodo = currentTodo.toDomain().copy(
                        status = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    // Update in local database first (immediate UI update)
                    updateTodoUseCase(updatedTodo)
                    android.util.Log.d("TodoViewModel", "Updated todo status: $id to $newStatus")
                }
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "Failed to update todo status", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update todo status"
                )
            }
        }
    }

    fun syncAllTodos() {
        viewModelScope.launch {
            try {
                syncTodosUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to sync todos"
                )
            }
        }
    }

    fun refreshFromNetwork() {
        viewModelScope.launch {
            syncFromNetwork()
        }
    }

    fun syncUnsyncedTodos() {
        viewModelScope.launch {
            try {
                syncTodosUseCase()
                android.util.Log.d("TodoViewModel", "Successfully synced all unsynced todos")
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "Failed to sync unsynced todos", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to sync todos: ${e.message}"
                )
            }
        }
    }

    fun testApiConnection() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TodoViewModel", "Testing API connection...")
                // This will test if we can fetch todos from API
                refreshTodosUseCase()
                android.util.Log.d("TodoViewModel", "API connection test successful")
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "API connection test failed", e)
                _uiState.value = _uiState.value.copy(
                    error = "API test failed: ${e.message}"
                )
            }
        }
    }

    fun testMinimalTodo() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TodoViewModel", "Testing with correct format todo data...")
                val testTodo = com.bashasoft.todo.data.model.TodoModel(
                    id = "",
                    title = "Test API",
                    description = "Testing API with correct format",
                    dueDate = "2025-12-31T00:00:00.000Z",
                    priority = "High",
                    status = "Not Started",
                    tags = emptyList()
                )
                addTodo(testTodo)
                android.util.Log.d("TodoViewModel", "Correct format todo test completed")
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "Correct format todo test failed", e)
                _uiState.value = _uiState.value.copy(
                    error = "Correct format test failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

// Extension functions for model conversion
private fun Todo.toUiModel(): com.bashasoft.todo.data.model.TodoModel {
    return com.bashasoft.todo.data.model.TodoModel(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        priority = priority.name,
        status = status.name,
        tags = tags
    )
}

private fun com.bashasoft.todo.data.model.TodoModel.toDomain(): Todo {
    return Todo(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        priority = when (priority.uppercase()) {
            "HIGH" -> Priority.HIGH
            "MEDIUM" -> Priority.MEDIUM
            "LOW" -> Priority.LOW
            else -> Priority.MEDIUM
        },
        status = when (status.uppercase()) {
            "NOT_STARTED", "NOT STARTED" -> Status.NOT_STARTED
            "IN_PROGRESS", "IN PROGRESS" -> Status.IN_PROGRESS
            "COMPLETED" -> Status.COMPLETED
            else -> Status.NOT_STARTED
        },
        tags = tags
    )
}
