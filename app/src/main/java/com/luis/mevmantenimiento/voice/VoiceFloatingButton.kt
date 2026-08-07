package com.luis.mevmantenimiento.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun VoiceFloatingButton(
    onTextoReconocido: (String) -> Unit,
    onError: (String) -> Unit,
    habilitado: Boolean = true
) {
    val context = LocalContext.current

    var modoContinuoActivo by remember {
        mutableStateOf(false)
    }

    var escuchando by remember {
        mutableStateOf(false)
    }

    val textoReconocidoActual by rememberUpdatedState(
        newValue = onTextoReconocido
    )

    val errorActual by rememberUpdatedState(
        newValue = onError
    )

    val voiceManager = remember {
        VoiceRecognitionManager(
            context = context,

            onTextoReconocido = { texto ->
                textoReconocidoActual(texto)
            },

            onEstadoCambio = { nuevoEstado ->
                escuchando = nuevoEstado
            },

            onError = { mensaje ->
                errorActual(mensaje)
            }
        )
    }

    val lanzadorPermiso =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { permisoConcedido ->

            if (permisoConcedido) {
                modoContinuoActivo = true
                voiceManager.iniciarModoContinuo()
            } else {
                modoContinuoActivo = false

                errorActual(
                    "Debes permitir el acceso al micrófono para usar los comandos de voz."
                )
            }
        }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.liberar()
        }
    }

    ExtendedFloatingActionButton(
        onClick = {
            if (!habilitado) {
                return@ExtendedFloatingActionButton
            }

            if (modoContinuoActivo) {
                modoContinuoActivo = false
                voiceManager.detenerModoContinuo()
                return@ExtendedFloatingActionButton
            }

            val permisoConcedido =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            if (permisoConcedido) {
                modoContinuoActivo = true
                voiceManager.iniciarModoContinuo()
            } else {
                lanzadorPermiso.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            }
        },

        containerColor = if (modoContinuoActivo) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },

        contentColor = if (modoContinuoActivo) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    ) {
        Row {
            Text(
                text = if (modoContinuoActivo) {
                    "●"
                } else {
                    "🎤"
                }
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = when {
                    modoContinuoActivo && escuchando ->
                        "Escuchando..."

                    modoContinuoActivo ->
                        "Voz activada"

                    else ->
                        "Activar voz"
                }
            )
        }
    }
}