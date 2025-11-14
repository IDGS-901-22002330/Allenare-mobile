package com.example.gemini_ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isFromUser: Boolean)

class GeminiViewModel : ViewModel() {

    // --- State for Single Question (Exercise Detail) ---
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse = _aiResponse.asStateFlow()

    // --- State for Chat Screen ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = "AIzaSyDxKVprZ2Z8d6ABHrmPwFO3JSQjHLcnGx0" // TODO: Replace with your actual API key
    )

    // --- Chat functionality ---
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") {
                text("Actúa como un entrenador personal experto y motivador llamado Allen. Tu objetivo es ayudarme a alcanzar mis metas de fitness con respuestas cortas pero efectivas. Proporciona consejos claros, seguros y personalizados. ¡Vamos a entrenar!")
            },
            content(role = "model") {
                text("¡Claro que sí! Soy Allen, tu entrenador personal de IA. Estoy listo para ayudarte a superar tus límites y alcanzar la mejor versión de ti mismo. ¿Qué te gustaría trabajar hoy? ¡Estoy aquí para ti!")
            }
        )
    )

    init {
        _chatMessages.value = listOf(ChatMessage("¡Claro que sí! Soy Allen, tu entrenador personal de IA. Estoy listo para ayudarte a superar tus límites y alcanzar la mejor versión de ti mismo. ¿Qué te gustaría trabajar hoy? ¡Estoy aquí para ti!", isFromUser = false))
    }

    fun sendMessage(userMessage: String) {
        // Add user message to UI
        _chatMessages.value = _chatMessages.value + ChatMessage(userMessage, isFromUser = true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(userMessage)
                response.text?.let {
                    _chatMessages.value = _chatMessages.value + ChatMessage(it, isFromUser = false)
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("Error: ${e.message}", isFromUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Single Question Functionality ---
    fun askQuestion(exerciseName: String, userQuestion: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = "Actúa como un entrenador personal experto. Responde a la siguiente pregunta sobre el ejercicio '$exerciseName' de forma clara, concisa y motivadora: $userQuestion"
                val response = generativeModel.generateContent(prompt)
                _aiResponse.value = response.text
            } catch (e: Exception) {
                _aiResponse.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}