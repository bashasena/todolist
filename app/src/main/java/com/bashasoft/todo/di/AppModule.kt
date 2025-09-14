package com.bashasoft.todo.di

import android.content.Context
import com.bashasoft.todo.data.local.TodoDatabase
import com.bashasoft.todo.data.local.TodoDao
import com.bashasoft.todo.data.remote.NetworkModule
import com.bashasoft.todo.data.remote.api.TodoApiService
import com.bashasoft.todo.data.repository.TodoRepositoryImpl
import com.bashasoft.todo.domain.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return TodoDatabase.getDatabase(context)
    }

    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    fun provideTodoApi(): TodoApiService {
        return NetworkModule.apiService
    }

    @Provides
    fun provideTodoRepository(
        api: TodoApiService,
        dao: TodoDao
    ): TodoRepository {
        return TodoRepositoryImpl(api, dao)
    }
}
