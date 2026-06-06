package com.damo.chatbot

import java.util.UUID

/**
 * Representa un mensaje individual en la conversación.
 */
data class ChatMessage(
    // ID único para eficiencia en la renderización de listas en Compose
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean, // True si lo envió el usuario, False si es del Bot
    val time: String // Hora formateada (ej: "10:35 AM")
)