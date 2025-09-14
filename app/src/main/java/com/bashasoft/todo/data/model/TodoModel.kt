package com.bashasoft.todo.data.model

import com.google.gson.annotations.SerializedName

data class TodoModel(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String, // ISO format
    val priority: String, // Enum recommended
    val status: String,
    val tags: List<String>
)
