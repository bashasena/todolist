package com.bashasoft.todo.domain.model

data class Todo(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: Priority,
    val status: Status,
    val tags: List<String>,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class Status {
    NOT_STARTED, IN_PROGRESS, COMPLETED
}
