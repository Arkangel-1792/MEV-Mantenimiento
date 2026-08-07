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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import com.luis.mevmantenimiento.voice.VoiceFloatingButton
import com.luis.mevmantenimiento.voice.VoiceCommandParser
import com.luis.mevmantenimiento.voice.VoiceCommand

@Composable
fun TomaHuellaScreen(
    activos: List<ActivoResumen>,
    guardando: Boolean,
    mensaje: String,
    onGuardar: (
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String,
        estadoRegistro: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var activoSeleccionado by remember {
        mutableStateOf<ActivoResumen?>(null)
    }

    var menuActivosExpandido by remember {
        mutableStateOf(false)
    }

    var proyecto by remember {
        mutableStateOf("")
    }

    var kilometraje by remember {
        mutableStateOf("")
    }

    var horometro by remember {
        mutableStateOf("")
    }

    var estadoGeneral by remember {
        mutableStateOf("")
    }

    var novedad by remember {
        mutableStateOf("")
    }

    var nombreTecnico by remember {
        mutableStateOf("")
    }

    var estadoRegistro by remember {
        mutableStateOf("BORRADOR")
    }

    val huellas = remember {
        mutableStateListOf(
            "", "", "", "", "", "",
            "", "", "", "", "", ""
        )
    }

    val cantidadPosiciones = obtenerCantidadPosiciones(
        activoSeleccionado
    )
    var textoReconocido by remember {
        mutableStateOf("")
    }

    var mensajeVoz by remember {
        mutableStateOf("")
    }

    fun guardarDesdeVoz(
        estado: String
    ) {
        val activo = activoSeleccionado

        if (activo == null) {
            mensajeVoz =
                "Primero debes seleccionar un activo."
            return
        }

        val huellasRegistro = List(12) { indice ->
            if (indice < cantidadPosiciones) {
                huellas[indice]
            } else {
                ""
            }
        }

        estadoRegistro = estado

        onGuardar(
            activo.codigo,
            proyecto,
            kilometraje,
            horometro,
            huellasRegistro,
            estadoGeneral,
            novedad,
            nombreTecnico,
            estado
        )
    }

    fun ejecutarComandoVoz(
        texto: String
    ) {
        when (
            val comando =
                VoiceCommandParser.interpretar(texto)
        ) {
            is VoiceCommand.SeleccionarActivo -> {

                val activoEncontrado =
                    activos.firstOrNull { activo ->
                        activo.codigo.equals(
                            comando.codigo,
                            ignoreCase = true
                        )
                    }

                if (activoEncontrado == null) {
                    mensajeVoz =
                        "No se encontró el activo ${comando.codigo}."
                    return
                }

                activoSeleccionado = activoEncontrado

                kilometraje =
                    activoEncontrado.kilometraje
                        ?.toString()
                        .orEmpty()

                horometro =
                    activoEncontrado.horometro
                        ?.toString()
                        .orEmpty()

                huellas.indices.forEach { indice ->
                    huellas[indice] = ""
                }

                mensajeVoz =
                    "Activo ${activoEncontrado.codigo} seleccionado."
            }

            is VoiceCommand.ActualizarPosicion -> {

                if (activoSeleccionado == null) {
                    mensajeVoz =
                        "Primero debes seleccionar un activo."
                    return
                }

                val indice =
                    comando.posicion - 1

                if (
                    indice < 0 ||
                    indice >= cantidadPosiciones
                ) {
                    mensajeVoz =
                        "La posición ${comando.posicion} no corresponde a este activo."
                    return
                }

                huellas[indice] = comando.valor

                mensajeVoz =
                    "P${comando.posicion} registrada con ${comando.valor} milímetros."
            }

            is VoiceCommand.ActualizarProyecto -> {
                proyecto = comando.valor

                mensajeVoz =
                    "Proyecto actualizado: ${comando.valor}."
            }

            is VoiceCommand.ActualizarKilometraje -> {
                kilometraje = comando.valor

                mensajeVoz =
                    "Kilometraje actualizado: ${comando.valor}."
            }

            is VoiceCommand.ActualizarHorometro -> {
                horometro = comando.valor

                mensajeVoz =
                    "Horómetro actualizado: ${comando.valor}."
            }

            is VoiceCommand.ActualizarEstadoGeneral -> {
                estadoGeneral = comando.valor

                mensajeVoz =
                    "Estado general actualizado."
            }

            is VoiceCommand.ActualizarNovedad -> {
                novedad = comando.valor

                mensajeVoz =
                    "Novedad actualizada."
            }

            VoiceCommand.GuardarBorrador -> {
                guardarDesdeVoz("BORRADOR")
            }

            VoiceCommand.EnviarRegistro -> {
                if (nombreTecnico.isBlank()) {
                    mensajeVoz =
                        "Ingresa el nombre del técnico antes de enviar."
                    return
                }

                guardarDesdeVoz("ENVIADO")
            }

            is VoiceCommand.Desconocido -> {
                mensajeVoz =
                    "No se entendió el comando. Inténtalo nuevamente."
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            VoiceFloatingButton(
                onTextoReconocido = { texto ->
                    textoReconocido = texto
                    mensajeVoz = ""

                    ejecutarComandoVoz(texto)
                },
                onError = { error ->
                    mensajeVoz = error
                },
                habilitado = !guardando
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 100.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Toma general de huella",
                style = MaterialTheme.typography.headlineSmall
            )

            if (textoReconocido.isNotBlank()) {
                Text(
                    text = "Comando reconocido:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = textoReconocido,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (mensajeVoz.isNotBlank()) {
                Text(
                    text = mensajeVoz,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedButton(
                onClick = {
                    menuActivosExpandido = true
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = activoSeleccionado?.let {
                        "${it.codigo} - ${it.subtipo}"
                    } ?: "Seleccionar activo"
                )
            }

            DropdownMenu(
                expanded = menuActivosExpandido,
                onDismissRequest = {
                    menuActivosExpandido = false
                }
            ) {
                activos.forEach { activo ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${activo.codigo} - ${activo.subtipo}"
                            )
                        },
                        onClick = {
                            activoSeleccionado = activo
                            menuActivosExpandido = false

                            kilometraje =
                                activo.kilometraje
                                    ?.toString()
                                    .orEmpty()

                            horometro =
                                activo.horometro
                                    ?.toString()
                                    .orEmpty()

                            huellas.indices.forEach { indice ->
                                huellas[indice] = ""
                            }
                        }
                    )
                }
            }

            if (activoSeleccionado != null) {
                Text(
                    text = "Posiciones habilitadas: $cantidadPosiciones",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = proyecto,
                onValueChange = {
                    proyecto = it
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
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
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Kilometraje")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = horometro,
                onValueChange = {
                    horometro = it
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Horómetro")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            if (activoSeleccionado != null) {
                Text(
                    text = "Profundidad de huella por posición",
                    style = MaterialTheme.typography.titleMedium
                )

                for (indice in 0 until cantidadPosiciones) {
                    OutlinedTextField(
                        value = huellas[indice],
                        onValueChange = {
                            huellas[indice] = it
                        },
                        enabled = !guardando,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("P${indice + 1} - Huella en mm")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )
                }
            }

            OutlinedTextField(
                value = estadoGeneral,
                onValueChange = {
                    estadoGeneral = it
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Estado general de las llantas")
                },
                minLines = 2
            )

            OutlinedTextField(
                value = novedad,
                onValueChange = {
                    novedad = it
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Novedad presentada")
                },
                minLines = 3
            )

            OutlinedTextField(
                value = nombreTecnico,
                onValueChange = {
                    nombreTecnico = it
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre del técnico")
                },
                singleLine = true
            )

            Text(
                text = "Estado del registro",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = estadoRegistro == "BORRADOR",
                    onClick = {
                        estadoRegistro = "BORRADOR"
                    },
                    enabled = !guardando,
                    label = {
                        Text("Borrador")
                    }
                )

                FilterChip(
                    selected = estadoRegistro == "ENVIADO",
                    onClick = {
                        estadoRegistro = "ENVIADO"
                    },
                    enabled = !guardando,
                    label = {
                        Text("Enviado")
                    }
                )
            }

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (guardando) {
                Text(
                    text = "Guardando información...",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val activo = activoSeleccionado
                        ?: return@Button

                    val huellasRegistro = List(12) { indice ->
                        if (indice < cantidadPosiciones) {
                            huellas[indice]
                        } else {
                            ""
                        }
                    }

                    onGuardar(
                        activo.codigo,
                        proyecto,
                        kilometraje,
                        horometro,
                        huellasRegistro,
                        estadoGeneral,
                        novedad,
                        nombreTecnico,
                        estadoRegistro
                    )
                },
                enabled =
                    !guardando &&
                            activoSeleccionado != null &&
                            nombreTecnico.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (estadoRegistro == "BORRADOR") {
                        "Guardar borrador"
                    } else {
                        "Enviar toma de huella"
                    }
                )
            }

            OutlinedButton(
                onClick = onVolver,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al menú principal")
            }
        }
    }
}

private fun obtenerCantidadPosiciones(
    activo: ActivoResumen?
): Int {
    if (activo == null) {
        return 0
    }

    val subtipo = activo.subtipo
        .trim()
        .uppercase()

    val tipo = activo.tipo
        .trim()
        .uppercase()

    val marca = activo.marca
        .trim()
        .uppercase()

    val codigo = activo.codigo
        .trim()
        .uppercase()

    return when {
        subtipo.contains("CAMIONETA") -> 4

        // Todas las volquetas SHACMAN usan 12 posiciones.
        (
                subtipo.contains("VOLQUETA") ||
                        tipo.contains("VOLQUETA") ||
                        codigo.startsWith("VVOLQ")
                ) &&
                marca.contains("SHACMAN") -> 12

        subtipo.contains("VOLQUETA") ||
                tipo.contains("VOLQUETA") ||
                codigo.startsWith("VVOLQ") -> 10

        subtipo.contains("CAMION") ||
                subtipo.contains("CAMIÓN") ||
                subtipo.contains("CABEZAL") ||
                subtipo.contains("TANQUERO") -> 6

        subtipo.contains("RODILLO") ||
                subtipo.contains("COMPACTADOR") -> 2

        subtipo.contains("RETROEXCAVADORA") ||
                subtipo.contains("MINICARGADORA") -> 4

        subtipo.contains("MOTONIVELADORA") -> 6

        else -> 4
    }
}
