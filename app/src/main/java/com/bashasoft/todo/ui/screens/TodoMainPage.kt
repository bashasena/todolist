package com.bashasoft.todo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bashasoft.todo.data.model.TodoModel
import com.bashasoft.todo.ui.theme.TodoTheme
import com.bashasoft.todo.viewmodel.TodoViewModel


@Composable
fun TaskRow(
    todo: TodoModel,
    onDeleteClick: (String?) -> Unit,
    onStatusChange: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.status.uppercase() == "COMPLETED",
                onCheckedChange = { isChecked ->
                    onStatusChange(todo.id, isChecked)
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (todo.description.isNotEmpty()) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (todo.priority.isNotEmpty()) {
                    Text(
                        text = "Priority: ${todo.priority}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(
                onClick = { onDeleteClick(todo.id) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TaskList(viewModel: TodoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Todo List",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            IconButton(onClick = { 
                viewModel.refreshFromNetwork()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh from Network",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(6.dp),
                    tint = Color.White
                )
            }
            IconButton(onClick = { 
                viewModel.syncUnsyncedTodos()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Unsynced Todos",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(6.dp),
                    tint = Color.White
                )
            }
            IconButton(onClick = { 
                viewModel.testApiConnection()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Test API Connection",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .padding(6.dp),
                    tint = Color.White
                )
            }
            IconButton(onClick = { 
                viewModel.testMinimalTodo()
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Test Minimal Todo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Yellow)
                        .padding(6.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { 
                showAddDialog = true
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    tint = Color.White
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.refreshFromNetwork() }) {
                        Text("Refresh from Network")
                    }
                }
            }
            uiState.todos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No todos found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 72.dp, 16.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.todos) { todo ->
                        TaskRow(
                            todo = todo,
                            onDeleteClick = { id ->
                                if (!id.isNullOrBlank()) {
                                    viewModel.deleteTodo(id)
                                }
                            },
                            onStatusChange = { id, isCompleted ->
                                viewModel.updateTodoStatus(id, isCompleted)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAddTodo = { todo -> viewModel.addTodo(todo) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
    TodoTheme {
        // Preview with sample data
        val sampleTodos = listOf(
            TodoModel(
                id = "1",
                title = "Go to masjid",
                description = "Friday prayer",
                dueDate = "2024-01-15",
                priority = "High",
                status = "pending",
                tags = listOf("religious", "weekly")
            ),
            TodoModel(
                id = "2",
                title = "Go to oasis",
                description = "Shopping trip",
                dueDate = "2024-01-16",
                priority = "Medium",
                status = "completed",
                tags = listOf("shopping", "leisure")
            )
        )
        // Note: This preview won't work with ViewModel, but shows the UI structure
    }
}