package com.bashasoft.todo.domain.usecase

import com.bashasoft.todo.domain.model.Todo
import com.bashasoft.todo.domain.repository.TodoRepository
import javax.inject.Inject

class UpdateTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo): Todo {
        return repository.updateTodo(todo)
    }
}
