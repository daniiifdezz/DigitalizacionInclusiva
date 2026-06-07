package org.dferna14.project.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow

class VoiceRecognizerAndroid(private val context: Context) : VoiceRecognizer {

    override val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private val _estado = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    override val estado = _estado

    private var recognizer: SpeechRecognizer? = null

    override fun iniciarEscucha() {
        if (!isAvailable) {
            _estado.value = VoiceRecognitionState.Error(
                "El reconocimiento de voz no está disponible en este dispositivo"
            )
            return
        }

        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            _estado.value = VoiceRecognitionState.Denegado
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _estado.value = VoiceRecognitionState.Escuchando
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onError(error: Int) {
                    val mensaje = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                        SpeechRecognizer.ERROR_CLIENT -> "Error interno"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos denegados"
                        SpeechRecognizer.ERROR_NETWORK -> "Sin conexión a internet"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera agotado"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna palabra"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                        SpeechRecognizer.ERROR_SERVER -> "Error del servidor de Google"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                        else -> "Error desconocido ($error)"
                    }
                    _estado.value = VoiceRecognitionState.Error(mensaje)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val texto = matches?.firstOrNull().orEmpty()
                    _estado.value = if (texto.isNotBlank()) {
                        VoiceRecognitionState.TextoReconocido(texto)
                    } else {
                        VoiceRecognitionState.Error("No se reconoció ninguna palabra")
                    }
                }
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer?.startListening(intent)
    }

    override fun pararEscucha() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        _estado.value = VoiceRecognitionState.Idle
    }
}

private lateinit var appContext: Context

fun registrarContextoVoz(context: Context) {
    appContext = context.applicationContext
}

actual fun crearVoiceRecognizer(): VoiceRecognizer = VoiceRecognizerAndroid(appContext)
