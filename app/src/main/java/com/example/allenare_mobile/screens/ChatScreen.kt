package com.example.allenare_mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val chatState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Para hacer scroll automático al último mensaje
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chat con Allenare AI", style = MaterialTheme.typography.headlineMedium)

        // --- Lista de Mensajes ---
        LazyColumn(
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            state = listState
        ) {
            items(chatState.messages) {
                MessageBubble(text = it.text, isFromUser = it.isFromUser)
            }
        }

        // --- Campo de Entrada y Botón ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = chatState.currentUserInput,
                onValueChange = { viewModel.onUserInputChange(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("¿Qué ejercicio quieres consultar?") },
                enabled = !chatState.isLoading
            )
            IconButton(onClick = { viewModel.sendMessage() }, enabled = !chatState.isLoading) {
                if (chatState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isFromUser: Boolean) {
    val background = if (isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val alignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .padding(12.dp),
            color = if (isFromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}