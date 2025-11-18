package com.example.allenare_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
private const val GEMINI_API_KEY = "AIzaSyDxKVprZ2Z8d6ABHrmPwFO3JSQjHLcnGx0"

// --- Clases para gestionar el estado del chat ---
data class ChatMessage(val text: String, val isFromUser: Boolean)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val currentUserInput: String = ""
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // -- INSTRUCCIÓN PARA LA IA --
    private val systemInstruction = content("system") {
        text("Eres un experto en fitness y entrenamiento personal. Tu nombre es Allenare AI. Debes responder a las preguntas de los usuarios sobre ejercicios, rutinas, nutrición y cualquier tema relacionado con el fitness. Sé claro, conciso y motivador.")
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash", // <-- NOMBRE CORREGIDO
        apiKey = GEMINI_API_KEY,
        systemInstruction = systemInstruction
    )

    fun onUserInputChange(text: String) {
        _uiState.value = _uiState.value.copy(currentUserInput = text)
    }

    fun sendMessage() {
        if (_uiState.value.currentUserInput.isBlank() || _uiState.value.isLoading) {
            return
        }

        val userMessage = ChatMessage(text = _uiState.value.currentUserInput, isFromUser = true)

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            currentUserInput = ""
        )

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(userMessage.text)
                val aiMessage = ChatMessage(text = response.text ?: "No se ha podido procesar la respuesta.", isFromUser = false)
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                val errorMessage = ChatMessage(text = "Error: ${e.localizedMessage}", isFromUser = false)
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false
                )
            }
        }
    }
}