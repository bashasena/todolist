package com.bashasoft.todo.data.mapper

import com.bashasoft.todo.data.local.TodoEntity
import com.bashasoft.todo.data.model.CreateTodoRequest
import com.bashasoft.todo.data.model.TodoModel
import com.bashasoft.todo.domain.model.Priority
import com.bashasoft.todo.domain.model.Status
import com.bashasoft.todo.domain.model.Todo

object TodoMapper {
    
    fun TodoEntity.toDomain(): Todo {
        return Todo(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority.toPriority(),
            status = status.toStatus(),
            tags = tags,
            isSynced = isSynced,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    fun Todo.toEntity(): TodoEntity {
        return TodoEntity(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority.name,
            status = status.name,
            tags = tags,
            isSynced = isSynced,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    fun TodoModel.toDomain(): Todo {
        return Todo(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority.fromApiPriority(),
            status = status.fromApiStatus(),
            tags = tags,
            isSynced = true // Network todos are considered synced
        )
    }
    
    fun Todo.toDataModel(): TodoModel {
        return TodoModel(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority.name,
            status = status.name,
            tags = tags
        )
    }
    
    fun Todo.toCreateRequest(): CreateTodoRequest {
        return CreateTodoRequest(
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority.toApiFormat(),
            status = status.toApiFormat(),
            tags = tags
        )
    }
    
    private fun String.toPriority(): Priority {
        return when (this.uppercase()) {
            "HIGH" -> Priority.HIGH
            "MEDIUM" -> Priority.MEDIUM
            "LOW" -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }
    
    private fun String.toStatus(): Status {
        return when (this.uppercase()) {
            "NOT_STARTED" -> Status.NOT_STARTED
            "IN_PROGRESS" -> Status.IN_PROGRESS
            "COMPLETED" -> Status.COMPLETED
            else -> Status.NOT_STARTED
        }
    }
    
    // Parse API format to internal enum format
    private fun String.fromApiPriority(): Priority {
        return when (this.lowercase()) {
            "high" -> Priority.HIGH
            "medium" -> Priority.MEDIUM
            "low" -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }
    
    private fun String.fromApiStatus(): Status {
        return when (this.lowercase()) {
            "not started" -> Status.NOT_STARTED
            "in progress" -> Status.IN_PROGRESS
            "completed" -> Status.COMPLETED
            else -> Status.NOT_STARTED
        }
    }
    
    // Convert enum values to API format
    fun Priority.toApiFormat(): String {
        return when (this) {
            Priority.HIGH -> "High"
            Priority.MEDIUM -> "Medium"
            Priority.LOW -> "Low"
        }
    }
    
    fun Status.toApiFormat(): String {
        return when (this) {
            Status.NOT_STARTED -> "Not Started"
            Status.IN_PROGRESS -> "In Progress"
            Status.COMPLETED -> "Completed"
        }
    }
}
