package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.luis.mevmantenimiento.voice.VoiceCommand
import com.luis.mevmantenimiento.voice.VoiceCommandParser
import com.luis.mevmantenimiento.voice.VoiceFloatingButton

@Composable
fun IntervencionLlantaScreen(
    activos: List<ActivoResumen>,
    guardando: Boolean,
    mensaje: String,
    onGuardarBorrador: (
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        tipoIntervencion: String,
        posicion: String,
        huella: String,
        marcaLlanta: String,
        medidaLlanta: String,
        serieLlanta: String,
        motivo: String,
        observaciones: String,
        nombreTecnico: String
    ) -> Unit,
    onEnviar: (
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        tipoIntervencion: String,
        posicion: String,
        huella: String,
        marcaLlanta: String,
        medidaLlanta: String,
        serieLlanta: String,
        motivo: String,
        observaciones: String,
        nombreTecnico: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var codigoActivo by remember { mutableStateOf("") }
    var proyecto by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }

    var tipoIntervencion by remember { mutableStateOf("CAMBIO") }
    var posicion by remember { mutableStateOf("") }
    var huella by remember { mutableStateOf("") }
    var marcaLlanta by remember { mutableStateOf("") }
    var medidaLlanta by remember { mutableStateOf("") }
    var serieLlanta by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var nombreTecnico by remember { mutableStateOf("") }

    var textoReconocido by remember { mutableStateOf("") }
    var mensajeVoz by remember { mutableStateOf("") }

    val activoEncontrado = activos.firstOrNull {
        it.codigo.equals(
            codigoActivo.trim(),
            ignoreCase = true
        )
    }

    fun guardarDesdeVoz(
        enviar: Boolean
    ) {
        val activoActual = activos.firstOrNull {
            it.codigo.equals(
                codigoActivo.trim(),
                ignoreCase = true
            )
        }

        if (activoActual == null) {
            mensajeVoz =
                "Primero debes seleccionar un activo válido."
            return
        }

        if (enviar) {
            if (tipoIntervencion.isBlank()) {
                mensajeVoz =
                    "Debes indicar el tipo de intervención."
                return
            }

            if (posicion.isBlank()) {
                mensajeVoz =
                    "Debes indicar la posición."
                return
            }

            if (nombreTecnico.isBlank()) {
                mensajeVoz =
                    "Debes indicar el nombre del técnico."
                return
            }

            onEnviar(
                codigoActivo,
                proyecto,
                kilometraje,
                horometro,
                tipoIntervencion,
                posicion,
                huella,
                marcaLlanta,
                medidaLlanta,
                serieLlanta,
                motivo,
                observaciones,
                nombreTecnico
            )

            mensajeVoz =
                "Orden de envío ejecutada."
        } else {
            onGuardarBorrador(
                codigoActivo,
                proyecto,
                kilometraje,
                horometro,
                tipoIntervencion,
                posicion,
                huella,
                marcaLlanta,
                medidaLlanta,
                serieLlanta,
                motivo,
                observaciones,
                nombreTecnico
            )

            mensajeVoz =
                "Orden de guardar borrador ejecutada."
        }
    }

    fun aplicarComandoVoz(
        comando: VoiceCommand
    ): String {

        return when (comando) {

            is VoiceCommand.SeleccionarActivo -> {
                val activo = activos.firstOrNull {
                    it.codigo.equals(
                        comando.codigo,
                        ignoreCase = true
                    )
                }

                if (activo == null) {
                    "No se encontró el activo ${comando.codigo}."
                } else {
                    codigoActivo = activo.codigo

                    kilometraje =
                        activo.kilometraje
                            ?.toString()
                            .orEmpty()

                    horometro =
                        activo.horometro
                            ?.toString()
                            .orEmpty()

                    "Activo ${activo.codigo} seleccionado."
                }
            }

            is VoiceCommand.ActualizarProyecto -> {
                proyecto = comando.valor
                "Proyecto: ${comando.valor}."
            }

            is VoiceCommand.ActualizarKilometraje -> {
                kilometraje = comando.valor
                "Kilometraje: ${comando.valor}."
            }

            is VoiceCommand.ActualizarHorometro -> {
                horometro = comando.valor
                "Horómetro: ${comando.valor}."
            }

            is VoiceCommand.ActualizarTipoIntervencion -> {
                tipoIntervencion = comando.valor
                "Intervención: ${comando.valor}."
            }

            is VoiceCommand.ActualizarPosicionLlanta -> {
                posicion = comando.posicion.toString()
                "Posición: ${comando.posicion}."
            }

            is VoiceCommand.ActualizarHuellaLlanta -> {
                huella = comando.valor
                "Huella: ${comando.valor} mm."
            }

            is VoiceCommand.ActualizarMarcaLlanta -> {
                marcaLlanta = comando.valor
                "Marca: ${comando.valor}."
            }

            is VoiceCommand.ActualizarMedidaLlanta -> {
                medidaLlanta = comando.valor
                "Medida: ${comando.valor}."
            }

            is VoiceCommand.ActualizarSerieLlanta -> {
                serieLlanta = comando.valor
                "Serie: ${comando.valor}."
            }

            is VoiceCommand.ActualizarMotivoIntervencion -> {
                motivo = comando.valor
                "Motivo actualizado."
            }

            is VoiceCommand.ActualizarNovedad -> {
                observaciones = comando.valor
                "Observaciones actualizadas."
            }

            is VoiceCommand.ActualizarTecnico -> {
                nombreTecnico = comando.valor
                "Técnico: ${comando.valor}."
            }

            VoiceCommand.GuardarBorrador -> {
                guardarDesdeVoz(
                    enviar = false
                )
                "Guardar borrador."
            }

            VoiceCommand.EnviarRegistro -> {
                guardarDesdeVoz(
                    enviar = true
                )
                "Enviar registro."
            }

            is VoiceCommand.Desconocido -> {
                "No se entendió: ${comando.textoOriginal}"
            }

            else -> {
                "Este comando no pertenece al formulario de intervención de llanta."
            }
        }
    }

    fun ejecutarComandoVoz(
        texto: String
    ) {
        textoReconocido = texto

        val comandos =
            VoiceCommandParser.interpretarVarios(
                texto
            )

        val respuestas =
            comandos.map { comando ->
                aplicarComandoVoz(comando)
            }

        mensajeVoz =
            respuestas.joinToString(
                separator = "\n"
            )
    }

    Scaffold(
        floatingActionButton = {
            VoiceFloatingButton(
                habilitado = !guardando,
                onTextoReconocido = { texto ->
                    ejecutarComandoVoz(texto)
                },
                onError = { error ->
                    mensajeVoz =
                        "Error de voz: $error"
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Intervención de llanta",
                style =
                    MaterialTheme.typography.headlineSmall
            )

            Text(
                text =
                    "Registra cambios, rotaciones, reparaciones, montajes, desmontajes o bajas.",
                style =
                    MaterialTheme.typography.bodyMedium
            )

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            if (mensajeVoz.isNotBlank()) {
                Text(
                    text = mensajeVoz,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            if (textoReconocido.isNotBlank()) {
                Text(
                    text = "Voz: $textoReconocido",
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = codigoActivo,
                onValueChange = {
                    codigoActivo = it.uppercase()
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Código del activo")
                },
                supportingText = {
                    when {
                        codigoActivo.isBlank() -> {
                            Text("Ejemplo: VVOLQ0099")
                        }

                        activoEncontrado != null -> {
                            Text(
                                "${activoEncontrado.subtipo} · " +
                                        "${activoEncontrado.marca} " +
                                        activoEncontrado.modelo
                            )
                        }

                        else -> {
                            Text(
                                "El código no consta en el catálogo."
                            )
                        }
                    }
                },
                singleLine = true
            )

            OutlinedTextField(
                value = proyecto,
                onValueChange = {
                    proyecto = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Proyecto")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = kilometraje,
                onValueChange = {
                    kilometraje = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Kilometraje")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),
                singleLine = true
            )

            OutlinedTextField(
                value = horometro,
                onValueChange = {
                    horometro = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Horómetro")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),
                singleLine = true
            )

            Text(
                text = "Tipo de intervención",
                style =
                    MaterialTheme.typography.titleMedium
            )

            val tipos = listOf(
                "CAMBIO",
                "ROTACIÓN",
                "REPARACIÓN",
                "MONTAJE",
                "DESMONTAJE",
                "BAJA"
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                tipos.chunked(2).forEach { fila ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { tipo ->
                            FilterChip(
                                selected =
                                    tipoIntervencion == tipo,
                                onClick = {
                                    tipoIntervencion = tipo
                                },
                                label = {
                                    Text(tipo)
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = posicion,
                onValueChange = {
                    posicion = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Posición")
                },
                supportingText = {
                    Text("Ejemplo: 3")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),
                singleLine = true
            )

            OutlinedTextField(
                value = huella,
                onValueChange = {
                    huella = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Huella (mm)")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),
                singleLine = true
            )

            OutlinedTextField(
                value = marcaLlanta,
                onValueChange = {
                    marcaLlanta = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Marca de llanta")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = medidaLlanta,
                onValueChange = {
                    medidaLlanta = it.uppercase()
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Medida de llanta")
                },
                supportingText = {
                    Text("Ejemplo: 12R22.5")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = serieLlanta,
                onValueChange = {
                    serieLlanta = it.uppercase()
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Serie / identificación")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = motivo,
                onValueChange = {
                    motivo = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Motivo")
                },
                minLines = 2
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = {
                    observaciones = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Observaciones")
                },
                minLines = 3
            )

            OutlinedTextField(
                value = nombreTecnico,
                onValueChange = {
                    nombreTecnico = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre del técnico")
                },
                singleLine = true
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    onGuardarBorrador(
                        codigoActivo,
                        proyecto,
                        kilometraje,
                        horometro,
                        tipoIntervencion,
                        posicion,
                        huella,
                        marcaLlanta,
                        medidaLlanta,
                        serieLlanta,
                        motivo,
                        observaciones,
                        nombreTecnico
                    )
                },
                enabled =
                    activoEncontrado != null &&
                            !guardando,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Guardar borrador")
            }

            Button(
                onClick = {
                    onEnviar(
                        codigoActivo,
                        proyecto,
                        kilometraje,
                        horometro,
                        tipoIntervencion,
                        posicion,
                        huella,
                        marcaLlanta,
                        medidaLlanta,
                        serieLlanta,
                        motivo,
                        observaciones,
                        nombreTecnico
                    )
                },
                enabled =
                    activoEncontrado != null &&
                            tipoIntervencion.isNotBlank() &&
                            posicion.isNotBlank() &&
                            nombreTecnico.isNotBlank() &&
                            !guardando,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Enviar registro")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Volver al menú principal")
            }
        }
    }
}