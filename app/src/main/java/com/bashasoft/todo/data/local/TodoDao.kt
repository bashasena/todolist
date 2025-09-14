package com.bashasoft.todo.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: String): TodoEntity?
    
    @Query("SELECT * FROM todos WHERE isSynced = 0")
    suspend fun getUnsyncedTodos(): List<TodoEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)
    
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    
    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: String)
    
    @Query("UPDATE todos SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE todos SET isSynced = 0 WHERE id = :id")
    suspend fun markAsUnsynced(id: String)
    
    @Query("UPDATE todos SET id = :newId WHERE id = :oldId")
    suspend fun updateTodoId(oldId: String, newId: String)
    
    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()
}
