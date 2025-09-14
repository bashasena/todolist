package com.bashasoft.todo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bashasoft.todo.data.model.TodoModel

@Entity(tableName = "todos")
@TypeConverters(TodoTypeConverters::class)
data class TodoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: String,
    val status: String,
    val tags: List<String>,
    val isSynced: Boolean = false, // Track sync status
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toTodoModel(): TodoModel {
        return TodoModel(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            status = status,
            tags = tags
        )
    }
    
    companion object {
        fun fromTodoModel(todo: TodoModel, isSynced: Boolean = false): TodoEntity {
            return TodoEntity(
                id = todo.id,
                title = todo.title,
                description = todo.description,
                dueDate = todo.dueDate,
                priority = todo.priority,
                status = todo.status,
                tags = todo.tags,
                isSynced = isSynced
            )
        }
    }
}

class TodoTypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
