package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.example.gemini_ai.ChatMessage
import com.example.gemini_ai.GeminiViewModel

@Composable
fun ChatScreen(geminiViewModel: GeminiViewModel = viewModel()) {
    val chatMessages by geminiViewModel.chatMessages.collectAsState()
    val isLoading by geminiViewModel.isLoading.collectAsState()

    ChatScreenContent(
        messages = chatMessages,
        isLoading = isLoading,
        onSendMessage = { geminiViewModel.sendMessage(it) }
    )
}

@Composable
fun ChatScreenContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit
) {
    var userMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to the bottom when a new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    ChatBubble(message = message)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessage,
                    onValueChange = { userMessage = it },
                    label = { Text("Pregúntale a Allen...") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (userMessage.isNotBlank()) {
                            onSendMessage(userMessage)
                            userMessage = ""
                        }
                    },
                    enabled = !isLoading && userMessage.isNotBlank(),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 0.dp,
                bottomEnd = if (message.isFromUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(16.dp),
                color = if (message.isFromUser) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val sampleMessages = listOf(
        ChatMessage("¡Hola! Soy Allen, tu entrenador personal de IA. ¡Estoy listo para ayudarte a superar tus límites! ¿Qué te gustaría trabajar hoy?", isFromUser = false),
        ChatMessage("Hola Allen, quiero empezar a correr pero no sé cómo.", isFromUser = true),
        ChatMessage("¡Excelente decisión! Correr es fantástico. Para empezar, te recomiendo un programa de intervalos: alterna 2 minutos de trote suave con 2 minutos de caminata. Repite esto 5 veces. ¿Qué te parece?", isFromUser = false),
        ChatMessage("Suena bien, lo intentaré mañana.", isFromUser = true)
    )
    AllenaremobileTheme {
        ChatScreenContent(
            messages = sampleMessages,
            isLoading = false,
            onSendMessage = {}
        )
    }
}
