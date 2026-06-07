package org.dferna14.project.util

import kotlinx.coroutines.flow.Flow

interface VoiceRecognizer {
    val isAvailable: Boolean
    val estado: Flow<VoiceRecognitionState>
    fun iniciarEscucha()
    fun pararEscucha()
}

sealed interface VoiceRecognitionState {
    object Idle : VoiceRecognitionState
    object Escuchando : VoiceRecognitionState
    data class TextoReconocido(val texto: String) : VoiceRecognitionState
    data class Error(val mensaje: String) : VoiceRecognitionState
    object Denegado : VoiceRecognitionState
}

expect fun crearVoiceRecognizer(): VoiceRecognizer
