package org.dferna14.project.util

import kotlinx.coroutines.flow.MutableStateFlow

class VoiceRecognizerJvm : VoiceRecognizer {
    override val isAvailable: Boolean = false
    override val estado = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    override fun iniciarEscucha() {}
    override fun pararEscucha() {}
}

actual fun crearVoiceRecognizer(): VoiceRecognizer = VoiceRecognizerJvm()
