package com.bashasoft.todo.data.model

import com.google.gson.annotations.SerializedName

data class CreateTodoResponseModel(
    val message: String,
    val data: CreateTodoDataModel
)

data class CreateTodoDataModel(
    @SerializedName("id")
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: String,
    val status: String,
    val tags: List<String>
)
