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
import androidx.compose.runtime.LaunchedEffect
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
fun NuevoMantenimientoScreen(
    activos: List<ActivoResumen>,
    guardandoMantenimiento: Boolean,
    mensajeMantenimiento: String,
    onGuardarBorrador: (
        codigoActivo: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String
    ) -> Unit,
    onEnviar: (
        codigoActivo: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var codigoActivo by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("PREVENTIVO") }
    var kilometraje by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    var accionEjecutada by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var ordenTrabajo by remember { mutableStateOf("") }
    var numeroPedido by remember { mutableStateOf("") }

    var textoReconocido by remember { mutableStateOf("") }
    var mensajeVoz by remember { mutableStateOf("") }

    val activoEncontrado = activos.firstOrNull {
        it.codigo.equals(codigoActivo.trim(), ignoreCase = true)
    }

    val errorIndicadores = validarIndicadoresActivo(
        activo = activoEncontrado,
        kilometraje = kilometraje,
        horometro = horometro
    )

    fun servicioPermitido(
        activo: ActivoResumen,
        servicio: String
    ): Boolean {
        return when (servicio.uppercase()) {
            "PREVENTIVO" -> activo.permitePreventivo
            "CORRECTIVO" -> activo.permiteCorrectivo
            else -> false
        }
    }

    LaunchedEffect(activoEncontrado?.codigo) {
        val activo = activoEncontrado ?: return@LaunchedEffect

        if (!activo.usaKilometraje) {
            kilometraje = ""
        }

        if (!activo.usaHorometro) {
            horometro = ""
        }

        if (!servicioPermitido(activo, tipoServicio)) {
            tipoServicio = if (activo.permitePreventivo) {
                "PREVENTIVO"
            } else {
                "CORRECTIVO"
            }
        }
    }

    fun guardarDesdeVoz(
        enviar: Boolean
    ): String {
        val activoActual = activos.firstOrNull {
            it.codigo.equals(codigoActivo.trim(), ignoreCase = true)
        }

        if (activoActual == null) {
            return "Primero debes seleccionar un activo válido."
        }

        if (!servicioPermitido(activoActual, tipoServicio)) {
            return "El activo no permite mantenimiento ${tipoServicio.lowercase()}."
        }

        if (enviar) {
            val errorIndicador = validarIndicadoresActivo(
                activo = activoActual,
                kilometraje = kilometraje,
                horometro = horometro
            )

            if (errorIndicador != null) {
                return errorIndicador
            }

            if (accionEjecutada.isBlank()) {
                return "Debes indicar la acción ejecutada antes de enviar."
            }

            onEnviar(
                codigoActivo,
                tipoServicio,
                kilometraje,
                horometro,
                accionEjecutada,
                observaciones,
                ordenTrabajo,
                numeroPedido
            )

            return "Orden de envío ejecutada."
        } else {
            onGuardarBorrador(
                codigoActivo,
                tipoServicio,
                kilometraje,
                horometro,
                accionEjecutada,
                observaciones,
                ordenTrabajo,
                numeroPedido
            )

            return "Orden de guardar borrador ejecutada."
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

                    kilometraje = if (activo.usaKilometraje) {
                        activo.kilometraje?.toString().orEmpty()
                    } else {
                        ""
                    }

                    horometro = if (activo.usaHorometro) {
                        activo.horometro?.toString().orEmpty()
                    } else {
                        ""
                    }

                    "Activo ${activo.codigo} seleccionado."
                }
            }

            is VoiceCommand.ActualizarTipoServicio -> {
                val activo = activoEncontrado
                if (
                    activo != null &&
                    !servicioPermitido(activo, comando.valor)
                ) {
                    "El activo no permite mantenimiento ${comando.valor.lowercase()}."
                } else {
                    tipoServicio = comando.valor
                    "Tipo de servicio: ${comando.valor}."
                }
            }

            is VoiceCommand.ActualizarKilometraje -> {
                kilometraje = comando.valor
                "Kilometraje: ${comando.valor}."
            }

            is VoiceCommand.ActualizarHorometro -> {
                horometro = comando.valor
                "Horómetro: ${comando.valor}."
            }

            is VoiceCommand.ActualizarAccionEjecutada -> {
                accionEjecutada = comando.valor
                "Acción ejecutada actualizada."
            }

            is VoiceCommand.ActualizarNovedad -> {
                observaciones = comando.valor
                "Observaciones actualizadas."
            }

            is VoiceCommand.ActualizarOrdenTrabajo -> {
                ordenTrabajo = comando.valor
                "Orden de trabajo: ${comando.valor}."
            }

            is VoiceCommand.ActualizarNumeroPedido -> {
                numeroPedido = comando.valor
                "Número de pedido: ${comando.valor}."
            }

            VoiceCommand.GuardarBorrador -> {
                guardarDesdeVoz(enviar = false)
            }

            VoiceCommand.EnviarRegistro -> {
                guardarDesdeVoz(enviar = true)
            }

            is VoiceCommand.Desconocido -> {
                "No se entendió: ${comando.textoOriginal}"
            }

            else -> {
                "Este comando no pertenece al formulario de mantenimiento."
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
                habilitado = !guardandoMantenimiento,
                onTextoReconocido = { texto ->
                    ejecutarComandoVoz(texto)
                },
                onError = { error ->
                    mensajeVoz = "Error de voz: $error"
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nuevo mantenimiento",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Registra una actividad preventiva o correctiva.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (mensajeMantenimiento.isNotBlank()) {
                Text(
                    text = mensajeMantenimiento,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (mensajeVoz.isNotBlank()) {
                Text(
                    text = mensajeVoz,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (textoReconocido.isNotBlank()) {
                Text(
                    text = "Voz: $textoReconocido",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (guardandoMantenimiento) {
                Text(
                    text = "No cierres la aplicación mientras se guarda el registro.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = codigoActivo,
                onValueChange = {
                    codigoActivo = it.uppercase()
                },
                modifier = Modifier.fillMaxWidth(),
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
                            Text("El código no consta en el catálogo.")
                        }
                    }
                },
                singleLine = true
            )

            Text(
                text = "Tipo de servicio",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tipoServicio == "PREVENTIVO",
                    onClick = {
                        tipoServicio = "PREVENTIVO"
                    },
                    label = {
                        Text("Preventivo")
                    },
                    enabled = activoEncontrado?.permitePreventivo != false
                )

                FilterChip(
                    selected = tipoServicio == "CORRECTIVO",
                    onClick = {
                        tipoServicio = "CORRECTIVO"
                    },
                    label = {
                        Text("Correctivo")
                    },
                    enabled = activoEncontrado?.permiteCorrectivo != false
                )
            }

            OutlinedTextField(
                value = kilometraje,
                onValueChange = {
                    kilometraje = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (activoEncontrado?.usaKilometraje == true) {
                            "Kilometraje *"
                        } else {
                            "Kilometraje"
                        }
                    )
                },
                supportingText = {
                    if (activoEncontrado?.usaKilometraje == false) {
                        Text("No aplica para el indicador de este activo")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                enabled =
                    activoEncontrado?.usaKilometraje != false &&
                            !guardandoMantenimiento,
                singleLine = true
            )

            OutlinedTextField(
                value = horometro,
                onValueChange = {
                    horometro = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (activoEncontrado?.usaHorometro == true) {
                            "Horómetro *"
                        } else {
                            "Horómetro"
                        }
                    )
                },
                supportingText = {
                    if (activoEncontrado?.usaHorometro == false) {
                        Text("No aplica para el indicador de este activo")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                enabled =
                    activoEncontrado?.usaHorometro != false &&
                            !guardandoMantenimiento,
                singleLine = true
            )

            OutlinedTextField(
                value = accionEjecutada,
                onValueChange = {
                    accionEjecutada = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (tipoServicio == "PREVENTIVO") {
                            "Mantenimiento realizado"
                        } else {
                            "Reparación o acción ejecutada"
                        }
                    )
                },
                minLines = 3
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = {
                    observaciones = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Observaciones")
                },
                minLines = 3
            )

            OutlinedTextField(
                value = ordenTrabajo,
                onValueChange = {
                    ordenTrabajo = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Orden de trabajo")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = numeroPedido,
                onValueChange = {
                    numeroPedido = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Número de pedido")
                },
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    onGuardarBorrador(
                        codigoActivo,
                        tipoServicio,
                        kilometraje,
                        horometro,
                        accionEjecutada,
                        observaciones,
                        ordenTrabajo,
                        numeroPedido
                    )
                },
                enabled = activoEncontrado != null &&
                        servicioPermitido(activoEncontrado, tipoServicio) &&
                        !guardandoMantenimiento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar borrador")
            }

            Button(
                onClick = {
                    onEnviar(
                        codigoActivo,
                        tipoServicio,
                        kilometraje,
                        horometro,
                        accionEjecutada,
                        observaciones,
                        ordenTrabajo,
                        numeroPedido
                    )
                },
                enabled = activoEncontrado != null &&
                        servicioPermitido(activoEncontrado, tipoServicio) &&
                        errorIndicadores == null &&
                        accionEjecutada.isNotBlank() &&
                        !guardandoMantenimiento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar registro")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al menú principal")
            }
        }
    }
}
