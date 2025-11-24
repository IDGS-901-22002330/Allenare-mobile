package com.example.gemini_ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini_ai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val generativeModel: GenerativeModel

    init {
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.API_KEY,
        )
        // Initial message from the assistant
        _chatMessages.value = listOf(
            ChatMessage(
                "¡Claro que sí! Soy Allen, tu entrenador personal de IA. Estoy listo para ayudarte a superar tus límites y alcanzar la mejor versión de ti mismo. ¿Qué te gustaría trabajar hoy? ¡Estoy aquí para ti!",
                isFromUser = false
            )
        )
    }

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

    fun sendMessage(userMessage: String) {
        // Add user message to the UI
        _chatMessages.update { it + ChatMessage(userMessage, isFromUser = true) }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(userMessage)
                response.text?.let { modelResponse ->
                    _chatMessages.update { it + ChatMessage(modelResponse, isFromUser = false) }
                }
            } catch (e: Exception) {
                _chatMessages.update { it + ChatMessage("Error: ${e.message}", isFromUser = false) }
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