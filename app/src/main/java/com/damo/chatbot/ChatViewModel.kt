package com.damo.chatbot

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel : ViewModel() {

    // 1. Iniciamos la sesión de chat con el modelo global
    // Esto mantiene el historial automático en la API de Gemini
    private val chatSession = generativeModel.startChat()

    // 2. Estado de la UI expuesto a Compose
    // mutableStateListOf avisa a Compose automáticamente cuando la lista cambia
    val messages = mutableStateListOf<ChatMessage>()

    // Estado para controlar si estamos esperando respuesta (para mostrar carga)
    var isLoading by mutableStateOf(false)
        private set // Solo el ViewModel puede modificar este estado

    // Estado para el texto que el usuario está escribiendo
    var userInputText by mutableStateOf("")

    init {
        // Mensaje de bienvenida inicial del bot

    }

    fun iniciarChat(promptOculto: String, nombreUsuario: String) {
        isLoading = true

        viewModelScope.launch {
            try {
                val response = chatSession.sendMessage(promptOculto)
                val botResponseText = response.text

                if (botResponseText != null) {
                    messages.add(
                        ChatMessage(
                            text = botResponseText.trim(),
                            isUser = false,
                            time = getCurrentFormattedTime()
                        )
                    )
                }
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        text = "¡Qué onda $nombreUsuario! Parece que no tengo conexión, pero aquí te espero. ✨",
                        isUser = false,
                        time = getCurrentFormattedTime()
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    // Función para actualizar el texto que escribe el usuario
    fun onUserInputChanged(newText: String) {
        userInputText = newText
    }

    /**
     * Lógica para enviar el mensaje del usuario y obtener respuesta de Gemini
     */
    fun sendMessage() {
        val textToSend = userInputText.trim()
        if (textToSend.isEmpty() || isLoading) return // Evitar enviar vacío o doble envío

        // A. Añadir mensaje del usuario a la lista local inmediatamente
        val userMessage = ChatMessage(
            text = textToSend,
            isUser = true,
            time = getCurrentFormattedTime()
        )
        messages.add(userMessage)

        // Limpiar el campo de entrada y setear estado de carga
        userInputText = ""
        isLoading = true

        // B. Enviar a Gemini de forma asíncrona usando Corrutinas
        viewModelScope.launch {
            try {
                // sendMessage suspende la ejecución hasta obtener respuesta
                val response = chatSession.sendMessage(textToSend)
                val botResponseText = response.text

                if (botResponseText != null) {
                    // C. Añadir respuesta del Bot a la lista local
                    messages.add(
                        ChatMessage(
                            text = botResponseText,
                            isUser = false,
                            time = getCurrentFormattedTime()
                        )
                    )
                } else {
                    // Manejar caso donde no hay texto de respuesta
                    addErrorMessage("Lo siento, no pude procesar una respuesta.")
                }

            } catch (e: Exception) {
                // D. Manejo de errores de conexión o API
                addErrorMessage("Error de conexión: ${e.localizedMessage ?: "Inténtalo de nuevo."}")
            } finally {
                // E. Finalizar estado de carga pase lo que pase
                isLoading = false
            }
        }
    }

    private fun addErrorMessage(errorText: String) {
        messages.add(
            ChatMessage(
                text = errorText,
                isUser = false,
                time = getCurrentFormattedTime()
            )
        )
    }

    // Función auxiliar para formatear la hora actual
    private fun getCurrentFormattedTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}