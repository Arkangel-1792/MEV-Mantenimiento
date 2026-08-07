package com.luis.mevmantenimiento.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceRecognitionManager(
    context: Context,
    private val onTextoReconocido: (String) -> Unit,
    private val onEstadoCambio: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {

    private val appContext =
        context.applicationContext

    private val handler =
        Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null

    private var modoContinuoActivo = false
    private var esperandoResultado = false
    private var liberado = false

    private val reconocimientoIntent =
        Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "es-EC"
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "es-EC"
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
            )
        }

    init {
        crearReconocedor()
    }

    private fun crearReconocedor() {
        if (liberado) {
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onError(
                "El reconocimiento de voz no está disponible en este dispositivo."
            )
            return
        }

        speechRecognizer?.destroy()

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(
                appContext
            )

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    onEstadoCambio(true)
                }

                override fun onBeginningOfSpeech() {
                    onEstadoCambio(true)
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) = Unit

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) = Unit

                override fun onEndOfSpeech() {
                    esperandoResultado = true
                }

                override fun onError(
                    error: Int
                ) {
                    esperandoResultado = false

                    if (!modoContinuoActivo) {
                        onEstadoCambio(false)
                        return
                    }

                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        SpeechRecognizer.ERROR_CLIENT -> {
                            reiniciarEscucha()
                        }

                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                            reiniciarEscucha(
                                demora = 800L
                            )
                        }

                        else -> {
                            onEstadoCambio(false)
                            onError(traducirError(error))
                            detenerModoContinuo()
                        }
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {
                    esperandoResultado = false

                    val resultados =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val texto =
                        resultados
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()

                    if (texto.isNotBlank()) {
                        onTextoReconocido(texto)
                    }

                    if (modoContinuoActivo) {
                        reiniciarEscucha()
                    } else {
                        onEstadoCambio(false)
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) = Unit

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) = Unit
            }
        )
    }

    fun iniciarModoContinuo() {
        if (liberado) {
            return
        }

        modoContinuoActivo = true
        iniciarNuevaEscucha()
    }

    fun detenerModoContinuo() {
        modoContinuoActivo = false
        esperandoResultado = false

        handler.removeCallbacksAndMessages(null)

        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
            // No requiere acción adicional.
        }

        onEstadoCambio(false)
    }

    fun alternarModoContinuo() {
        if (modoContinuoActivo) {
            detenerModoContinuo()
        } else {
            iniciarModoContinuo()
        }
    }

    fun estaEnModoContinuo(): Boolean {
        return modoContinuoActivo
    }

    private fun iniciarNuevaEscucha() {
        if (
            liberado ||
            !modoContinuoActivo ||
            esperandoResultado
        ) {
            return
        }

        try {
            speechRecognizer?.cancel()

            handler.postDelayed(
                {
                    if (
                        modoContinuoActivo &&
                        !liberado
                    ) {
                        try {
                            speechRecognizer?.startListening(
                                reconocimientoIntent
                            )

                            onEstadoCambio(true)
                        } catch (error: Exception) {
                            onEstadoCambio(false)

                            onError(
                                "No se pudo iniciar el micrófono: " +
                                        error.message.orEmpty()
                            )

                            detenerModoContinuo()
                        }
                    }
                },
                250L
            )
        } catch (error: Exception) {
            onEstadoCambio(false)

            onError(
                "No se pudo preparar el micrófono: " +
                        error.message.orEmpty()
            )

            detenerModoContinuo()
        }
    }

    private fun reiniciarEscucha(
        demora: Long = 400L
    ) {
        if (
            liberado ||
            !modoContinuoActivo
        ) {
            return
        }

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(
            {
                esperandoResultado = false
                iniciarNuevaEscucha()
            },
            demora
        )
    }

    fun liberar() {
        liberado = true
        modoContinuoActivo = false
        esperandoResultado = false

        handler.removeCallbacksAndMessages(null)

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } finally {
            speechRecognizer = null
            onEstadoCambio(false)
        }
    }

    private fun traducirError(
        codigoError: Int
    ): String {
        return when (codigoError) {
            SpeechRecognizer.ERROR_AUDIO ->
                "Se presentó un problema con el micrófono."

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "La aplicación no tiene permiso para usar el micrófono."

            SpeechRecognizer.ERROR_NETWORK ->
                "No fue posible conectarse al servicio de reconocimiento."

            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "El servicio de reconocimiento tardó demasiado."

            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "El reconocimiento de voz está ocupado."

            SpeechRecognizer.ERROR_SERVER ->
                "El servicio de reconocimiento no respondió."

            else ->
                "Ocurrió un error durante el reconocimiento de voz."
        }
    }
}