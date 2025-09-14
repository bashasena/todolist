package com.bashasoft.todo.domain.usecase

import com.bashasoft.todo.domain.repository.TodoRepository
import javax.inject.Inject

class DeleteTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteTodo(id)
    }
}
