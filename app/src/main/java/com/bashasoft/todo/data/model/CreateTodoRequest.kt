package com.bashasoft.todo.data.model

data class CreateTodoRequest(
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: String,
    val status: String,
    val tags: List<String>
)
