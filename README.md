
## 📱 **Todo App Complete Flow Diagram**

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🚀 APP STARTUP FLOW                                   │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 📱 User Launches App
   ↓
2. 🏗️ Android System Creates TodoApplication
   ↓
3. �� @HiltAndroidApp Annotation Triggers Hilt DI Setup
   ↓
4. �� AppModule Provides Dependencies:
   • TodoDatabase (Room)
   • TodoDao (Database Access)
   • TodoApiService (Retrofit)
   • TodoRepository (Data Layer)
   ↓
5. 🎬 MainActivity.onCreate() Executes
   ↓
6. �� TodoViewModel Created via @AndroidEntryPoint
   ↓
7. 🎨 Compose UI Setup (TaskList Screen)
   ↓
8. ⚡ ViewModel.init() → loadTodos() Triggered
   ↓
9. 🔄 syncFromNetwork() Called (Background)
   ↓
10. �� getAllTodosUseCase() → Flow.collect() Starts
    ↓
11. 🎯 UI State Updates → TaskList Renders

┌─────────────────────────────────────────────────────────────────────────────────┐
│                        🔄 NETWORK SYNC FLOW (Startup)                          │
└─────────────────────────────────────────────────────────────────────────────────┘

syncFromNetwork() → refreshTodosUseCase() → repository.fetchTodosFromNetwork()
   ↓
📡 API Call: GET https://shrimo.com/fake-api/todos
   ↓
�� Response: List<TodoModel> with "_id" field
   ↓
🔄 TodoModel.toDomain() → Status.fromApiStatus() → Priority.fromApiPriority()
   ↓
💾 todoDao.insertTodos() → Local Database Updated
   ↓
📊 Flow Emits → UI Updates Automatically

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ➕ ADD TODO FLOW                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

1. �� User Clicks "Add Todo" Button
   ↓
2. 🎨 AddTodoDialog Opens
   ↓
3. 📝 User Fills Form (Title, Description, Due Date, Priority, Status)
   ↓
4. ✅ User Clicks "Add" Button
   ↓
5. 🎯 viewModel.addTodo(todoModel) Called
   ↓
6. 🔄 todoModel.toDomain() → Convert to Domain Model
   ↓
7. �� addTodoUseCase(domainTodo) → Repository.addTodo()
   ↓
8. 🆔 Generate UUID if ID Empty
   ↓
9. 💾 todoDao.insertTodo() → Save to Local DB (isSynced = false)
   ↓
10. ⚡ UI Updates Immediately (Flow.collect())
    ↓
11. �� syncTodoToNetwork() → Background Sync
    ↓
12. 📡 API Call: POST https://shrimo.com/fake-api/todos
    ↓
13. 📥 Response: CreateTodoResponseModel with "id" field
    ↓
14. �� Update Local ID: todoDao.updateTodoId(oldId, newId)
    ↓
15. ✅ todoDao.markAsSynced() → Mark as Synced

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🗑️ DELETE TODO FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Delete Icon (🗑️)
   ↓
2. �� viewModel.deleteTodo(id) Called
   ↓
3. 📦 deleteTodoUseCase(id) → Repository.deleteTodo()
   ↓
4. 🔍 todoDao.getTodoById(id) → Check if Synced
   ↓
5. �� IF Synced: API Call DELETE https://shrimo.com/fake-api/todos/{id}
    ↓
6. �� todoDao.deleteTodoById(id) → Delete from Local DB
    ↓
7. ⚡ UI Updates Immediately (Flow.collect())

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ✅ STATUS CHANGE FLOW                                │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Checkbox
   ↓
2. 🎯 TaskRow.onStatusChange(id, isCompleted) Called
   ↓
3. 🔄 viewModel.updateTodoStatus(id, isCompleted)
   ↓
4. 🔍 Find Current Todo in UI State
   ↓
5. �� Determine New Status:
   • isChecked = true → Status.COMPLETED
   • isChecked = false → Status.NOT_STARTED
   ↓
6. 📦 updateTodoUseCase(updatedTodo) → Repository.updateTodo()
   ↓
7. �� todoDao.updateTodo() → Update Local DB (isSynced = false)
   ↓
8. ⚡ UI Updates Immediately (Flow.collect())
   ↓
9. 🔄 syncTodoUpdateToNetwork() → Background Sync
   ↓
10. 📡 API Call: POST https://shrimo.com/fake-api/todos (Same as Create)
    ↓
11. ✅ todoDao.markAsSynced() → Mark as Synced

┌─────────────────────────────────────────────────────────────────────────────────┐
│                        🔄 SYNC ALL TODOS FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Sync Button
   ↓
2. 🎯 viewModel.syncAllTodos() Called
   ↓
3. 📦 syncTodosUseCase() → Repository.syncAllTodos()
   ↓
4. 🔍 todoDao.getUnsyncedTodos() → Get Unsynced Todos
   ↓
5. 🔄 For Each Unsynced Todo: syncTodoToNetwork()
   ↓
6. �� API Calls: POST to Create/Update Todos
   ↓
7. ✅ Mark All as Synced

┌─────────────────────────────────────────────────────────────────────────────────┐
│                        🔄 REFRESH FROM NETWORK FLOW                           │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Refresh Button
   ↓
2. 🎯 viewModel.refreshFromNetwork() Called
   ↓
3. 🔄 syncFromNetwork() → refreshTodosUseCase()
   ↓
4. 📡 API Call: GET https://shrimo.com/fake-api/todos
   ↓
5. �� Response: Latest Todos from Server
   ↓
6. �� todoDao.insertTodos() → Update Local DB (Replace All)
   ↓
7. ⚡ UI Updates with Latest Data

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🏗️ ARCHITECTURE LAYERS                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   �� UI Layer   │    │ �� Domain Layer │    │ �� Data Layer   │
│                 │    │                 │    │                 │
│ • MainActivity  │◄──►│ • Use Cases     │◄──►│ • Repository    │
│ • TaskList      │    │ • Models        │    │ • API Service   │
│ • AddTodoDialog │    │ • Repository    │    │ • Local DB      │
│ • TodoViewModel │    │   Interface     │    │ • Mappers       │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🔄 DATA FLOW PATTERN                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

UI Action → ViewModel → UseCase → Repository → Data Source
    ↑                                                      ↓
    └────────────── Flow.collect() ←─── UI State ←────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           📡 NETWORK & SYNC STRATEGY                           │
└─────────────────────────────────────────────────────────────────────────────────┘

1. �� **Offline-First**: All operations work locally first
2. ⚡ **Immediate UI**: Local changes reflect instantly
3. �� **Background Sync**: Network operations happen in background
4. �� **Retry Logic**: Failed syncs marked as unsynced for retry
5. 🆔 **ID Mapping**: Local UUIDs mapped to server MongoDB IDs
6. 📊 **Flow-Based**: Reactive UI updates via Kotlin Flows

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🎯 KEY TECHNOLOGIES                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

• 🏗️ **Architecture**: MVVM + Clean Architecture
• 🔧 **DI**: Hilt (Dagger)
• 🎨 **UI**: Jetpack Compose
• 💾 **Local DB**: Room
• 📡 **Network**: Retrofit + OkHttp
• 🔄 **Async**: Kotlin Coroutines + Flows
• 🎯 **Pattern**: Repository Pattern
• 📊 **State**: StateFlow + MutableStateFlow
```

This comprehensive flow diagram shows:

1. **🚀 App Startup** - From launch to UI rendering
2. **➕ Add Todo** - Complete creation flow with sync
3. **🗑️ Delete Todo** - Deletion with network sync
4. **✅ Status Change** - Checkbox status updates
5. **🔄 Sync Operations** - Background synchronization
6. **🏗️ Architecture** - Layer separation and dependencies
7. **�� Network Strategy** - Offline-first with background sync

The app follows a **reactive, offline-first architecture** where all operations work locally first, then sync to the network in the background, ensuring a smooth user experience even with poor connectivity! 🚀

```plaintext
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🚀 APP STARTUP FLOW                                   │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 📱 User Launches App
   ↓
2. 🏗️ Android System Creates TodoApplication
   ↓
3. �� @HiltAndroidApp Annotation Triggers Hilt DI Setup
   ↓
4. �� AppModule Provides Dependencies:
   • TodoDatabase (Room)
   • TodoDao (Database Access)
   • TodoApiService (Retrofit)
   • TodoRepository (Data Layer)
   ↓
5. 🎬 MainActivity.onCreate() Executes
   ↓
6. �� TodoViewModel Created via @AndroidEntryPoint
   ↓
7. 🎨 Compose UI Setup (TaskList Screen)
   ↓
8. ⚡ ViewModel.init() → loadTodos() Triggered
   ↓
9. 🔄 syncFromNetwork() Called (Background)
   ↓
10. �� getAllTodosUseCase() → Flow.collect() Starts
    ↓
11. 🎯 UI State Updates → TaskList Renders

┌─────────────────────────────────────────────────────────────────────────────────┐
│                        🔄 NETWORK SYNC FLOW (Startup)                          │
└─────────────────────────────────────────────────────────────────────────────────┘

syncFromNetwork() → refreshTodosUseCase() → repository.fetchTodosFromNetwork()
   ↓
📡 API Call: GET https://shrimo.com/fake-api/todos
   ↓
�� Response: List<TodoModel> with "_id" field
   ↓
🔄 TodoModel.toDomain() → Status.fromApiStatus() → Priority.fromApiPriority()
   ↓
💾 todoDao.insertTodos() → Local Database Updated
   ↓
📊 Flow Emits → UI Updates Automatically

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ➕ ADD TODO FLOW                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

1. �� User Clicks "Add Todo" Button
   ↓
2. 🎨 AddTodoDialog Opens
   ↓
3. 📝 User Fills Form (Title, Description, Due Date, Priority, Status)
   ↓
4. ✅ User Clicks "Add" Button
   ↓
5. 🎯 viewModel.addTodo(todoModel) Called
   ↓
6. 🔄 todoModel.toDomain() → Convert to Domain Model
   ↓
7. �� addTodoUseCase(domainTodo) → Repository.addTodo()
   ↓
8. 🆔 Generate UUID if ID Empty
   ↓
9. 💾 todoDao.insertTodo() → Save to Local DB (isSynced = false)
   ↓
10. ⚡ UI Updates Immediately (Flow.collect())
    ↓
11. �� syncTodoToNetwork() → Background Sync
    ↓
12. 📡 API Call: POST https://shrimo.com/fake-api/todos
    ↓
13. 📥 Response: CreateTodoResponseModel with "id" field
    ↓
14. �� Update Local ID: todoDao.updateTodoId(oldId, newId)
    ↓
15. ✅ todoDao.markAsSynced() → Mark as Synced

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🗑️ DELETE TODO FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Delete Icon (🗑️)
   ↓
2. �� viewModel.deleteTodo(id) Called
   ↓
3. 📦 deleteTodoUseCase(id) → Repository.deleteTodo()
   ↓
4. 🔍 todoDao.getTodoById(id) → Check if Synced
   ↓
5. �� IF Synced: API Call DELETE https://shrimo.com/fake-api/todos/{id}
    ↓
6. �� todoDao.deleteTodoById(id) → Delete from Local DB
    ↓
7. ⚡ UI Updates Immediately (Flow.collect())

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ✅ STATUS CHANGE FLOW                                │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Checkbox
   ↓
2. 🎯 TaskRow.onStatusChange(id, isCompleted) Called
   ↓
3. 🔄 viewModel.updateTodoStatus(id, isCompleted)
   ↓
4. 🔍 Find Current Todo in UI State
   ↓
5. �� Determine New Status:
   • isChecked = true → Status.COMPLETED
   • isChecked = false → Status.NOT_STARTED
   ↓
6. 📦 updateTodoUseCase(updatedTodo) → Repository.updateTodo()
   ↓
7. �� todoDao.updateTodo() → Update Local DB (isSynced = false)
   ↓
8. ⚡ UI Updates Immediately (Flow.collect())
   ↓
9. 🔄 syncTodoUpdateToNetwork() → Background Sync
   ↓
10. 📡 API Call: POST https://shrimo.com/fake-api/todos (Same as Create)
    ↓
11. ✅ todoDao.markAsSynced() → Mark as Synced

┌─────────────────────────────────────────────────────────────────────────────────┐
│                        🔄 SYNC ALL TODOS FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Sync Button
   ↓
2. 🎯 viewModel.syncAllTodos() Called
   ↓
3. 📦 syncTodosUseCase() → Repository.syncAllTodos()
   ↓
4. 🔍 todoDao.getUnsyncedTodos() → Get Unsynced Todos
   ↓
5. 🔄 For Each Unsynced Todo: syncTodoToNetwork()
   ↓
6. �� API Calls: POST to Create/Update Todos
   ↓
7. ✅ Mark All as Synced

┌─────────────────────────────────────────────────────────────────────────────────┐
│                        🔄 REFRESH FROM NETWORK FLOW                           │
└─────────────────────────────────────────────────────────────────────────────────┘

1. 👤 User Clicks Refresh Button
   ↓
2. 🎯 viewModel.refreshFromNetwork() Called
   ↓
3. 🔄 syncFromNetwork() → refreshTodosUseCase()
   ↓
4. 📡 API Call: GET https://shrimo.com/fake-api/todos
   ↓
5. �� Response: Latest Todos from Server
   ↓
6. �� todoDao.insertTodos() → Update Local DB (Replace All)
   ↓
7. ⚡ UI Updates with Latest Data

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🏗️ ARCHITECTURE LAYERS                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   �� UI Layer   │    │ �� Domain Layer │    │ �� Data Layer   │
│                 │    │                 │    │                 │
│ • MainActivity  │◄──►│ • Use Cases     │◄──►│ • Repository    │
│ • TaskList      │    │ • Models        │    │ • API Service   │
│ • AddTodoDialog │    │ • Repository    │    │ • Local DB      │
│ • TodoViewModel │    │   Interface     │    │ • Mappers       │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🔄 DATA FLOW PATTERN                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

UI Action → ViewModel → UseCase → Repository → Data Source
    ↑                                                      ↓
    └────────────── Flow.collect() ←─── UI State ←────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           📡 NETWORK & SYNC STRATEGY                           │
└─────────────────────────────────────────────────────────────────────────────────┘

1. �� **Offline-First**: All operations work locally first
2. ⚡ **Immediate UI**: Local changes reflect instantly
3. �� **Background Sync**: Network operations happen in background
4. �� **Retry Logic**: Failed syncs marked as unsynced for retry
5. 🆔 **ID Mapping**: Local UUIDs mapped to server MongoDB IDs
6. 📊 **Flow-Based**: Reactive UI updates via Kotlin Flows

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           🎯 KEY TECHNOLOGIES                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

• 🏗️ **Architecture**: MVVM + Clean Architecture
• 🔧 **DI**: Hilt (Dagger)
• 🎨 **UI**: Jetpack Compose
• 💾 **Local DB**: Room
• 📡 **Network**: Retrofit + OkHttp
• 🔄 **Async**: Kotlin Coroutines + Flows
• 🎯 **Pattern**: Repository Pattern
• 📊 **State**: StateFlow + MutableStateFlow
```

