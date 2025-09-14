package com.bashasoft.todo.ui

import com.bashasoft.todo.data.model.TodoModel

data class TodoUiState(
    val isLoading: Boolean = false,
    val todos: List<TodoModel> = emptyList(),
    val error: String? = null
)
