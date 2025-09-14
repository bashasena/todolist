package com.bashasoft.todo.domain.usecase

import com.bashasoft.todo.domain.model.Todo
import com.bashasoft.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> {
        return repository.getAllTodos()
    }
}
