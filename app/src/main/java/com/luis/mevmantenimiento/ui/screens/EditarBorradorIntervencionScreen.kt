package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
fun EditarBorradorIntervencionScreen(
    borrador: BorradorIntervencionLlanta,
    activos: List<ActivoResumen>,
    guardando: Boolean,
    mensaje: String,
    onActualizarBorrador: (
        String, String, String, String, String, String, String,
        String, String, String, String, String, String, String
    ) -> Unit,
    onEnviarBorrador: (
        String, String, String, String, String, String, String,
        String, String, String, String, String, String, String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var codigoActivo by remember { mutableStateOf(borrador.codigoActivo) }
    var proyecto by remember { mutableStateOf(borrador.proyecto) }
    var kilometraje by remember { mutableStateOf(borrador.kilometraje?.toString().orEmpty()) }
    var horometro by remember { mutableStateOf(borrador.horometro?.toString().orEmpty()) }
    var tipoIntervencion by remember { mutableStateOf(borrador.tipoIntervencion.ifBlank { "CAMBIO" }) }
    var posicion by remember { mutableStateOf(borrador.posicion) }
    var huella by remember { mutableStateOf(borrador.huella?.toString().orEmpty()) }
    var marcaLlanta by remember { mutableStateOf(borrador.marcaLlanta) }
    var medidaLlanta by remember { mutableStateOf(borrador.medidaLlanta) }
    var serieLlanta by remember { mutableStateOf(borrador.serieLlanta) }
    var motivo by remember { mutableStateOf(borrador.motivo) }
    var observaciones by remember { mutableStateOf(borrador.observaciones) }
    var nombreTecnico by remember { mutableStateOf(borrador.nombreTecnico) }
    var mensajeVoz by remember { mutableStateOf("") }

    fun aplicar(comando: VoiceCommand): String {
        return when (comando) {
            is VoiceCommand.SeleccionarActivo -> {
                val activo = activos.firstOrNull {
                    it.codigo.equals(comando.codigo, ignoreCase = true)
                }
                if (activo == null) {
                    "Activo no encontrado."
                } else {
                    codigoActivo = activo.codigo
                    kilometraje = activo.kilometraje?.toString().orEmpty()
                    horometro = activo.horometro?.toString().orEmpty()
                    "Activo actualizado."
                }
            }
            is VoiceCommand.ActualizarProyecto -> {
                proyecto = comando.valor
                "Proyecto actualizado."
            }
            is VoiceCommand.ActualizarKilometraje -> {
                kilometraje = comando.valor
                "Kilometraje actualizado."
            }
            is VoiceCommand.ActualizarHorometro -> {
                horometro = comando.valor
                "Horómetro actualizado."
            }
            is VoiceCommand.ActualizarTipoIntervencion -> {
                tipoIntervencion = comando.valor
                "Intervención actualizada."
            }
            is VoiceCommand.ActualizarPosicionLlanta -> {
                posicion = comando.posicion.toString()
                "Posición actualizada."
            }
            is VoiceCommand.ActualizarHuellaLlanta -> {
                huella = comando.valor
                "Huella actualizada."
            }
            is VoiceCommand.ActualizarMarcaLlanta -> {
                marcaLlanta = comando.valor
                "Marca actualizada."
            }
            is VoiceCommand.ActualizarMedidaLlanta -> {
                medidaLlanta = comando.valor
                "Medida actualizada."
            }
            is VoiceCommand.ActualizarSerieLlanta -> {
                serieLlanta = comando.valor
                "Serie actualizada."
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
                "Técnico actualizado."
            }
            else -> "Comando no aplicable."
        }
    }

    fun ejecutarVoz(texto: String) {
        mensajeVoz = VoiceCommandParser
            .interpretarVarios(texto)
            .map { aplicar(it) }
            .joinToString("\n")
    }

    Scaffold(
        floatingActionButton = {
            VoiceFloatingButton(
                habilitado = !guardando,
                onTextoReconocido = { ejecutarVoz(it) },
                onError = { mensajeVoz = "Error de voz: $it" }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Editar intervención",
                style = MaterialTheme.typography.headlineSmall
            )

            if (borrador.estadoRegistro == "DEVUELTO") {
                Text(
                    text = "Motivo de devolución: ${borrador.motivoDevolucion.ifBlank { "Sin detalle" }}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (mensaje.isNotBlank()) Text(mensaje)
            if (mensajeVoz.isNotBlank()) Text(mensajeVoz)

            OutlinedTextField(codigoActivo, { codigoActivo = it.uppercase() }, Modifier.fillMaxWidth(), label = { Text("Código del activo") })
            OutlinedTextField(proyecto, { proyecto = it }, Modifier.fillMaxWidth(), label = { Text("Proyecto") })
            OutlinedTextField(kilometraje, { kilometraje = it }, Modifier.fillMaxWidth(), label = { Text("Kilometraje") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(horometro, { horometro = it }, Modifier.fillMaxWidth(), label = { Text("Horómetro") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            Text("Tipo de intervención", style = MaterialTheme.typography.titleMedium)

            listOf(
                "CAMBIO", "ROTACIÓN", "REPARACIÓN",
                "MONTAJE", "DESMONTAJE", "BAJA"
            ).chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fila.forEach { tipo ->
                        FilterChip(
                            selected = tipoIntervencion == tipo,
                            onClick = { tipoIntervencion = tipo },
                            label = { Text(tipo) }
                        )
                    }
                }
            }

            OutlinedTextField(posicion, { posicion = it }, Modifier.fillMaxWidth(), label = { Text("Posición") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(huella, { huella = it }, Modifier.fillMaxWidth(), label = { Text("Huella (mm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(marcaLlanta, { marcaLlanta = it }, Modifier.fillMaxWidth(), label = { Text("Marca") })
            OutlinedTextField(medidaLlanta, { medidaLlanta = it.uppercase() }, Modifier.fillMaxWidth(), label = { Text("Medida") })
            OutlinedTextField(serieLlanta, { serieLlanta = it.uppercase() }, Modifier.fillMaxWidth(), label = { Text("Serie") })
            OutlinedTextField(motivo, { motivo = it }, Modifier.fillMaxWidth(), label = { Text("Motivo") }, minLines = 2)
            OutlinedTextField(observaciones, { observaciones = it }, Modifier.fillMaxWidth(), label = { Text("Observaciones") }, minLines = 3)
            OutlinedTextField(nombreTecnico, { nombreTecnico = it }, Modifier.fillMaxWidth(), label = { Text("Técnico") })

            Button(
                onClick = {
                    onActualizarBorrador(
                        borrador.id, codigoActivo, proyecto, kilometraje, horometro,
                        tipoIntervencion, posicion, huella, marcaLlanta, medidaLlanta,
                        serieLlanta, motivo, observaciones, nombreTecnico
                    )
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            Button(
                onClick = {
                    onEnviarBorrador(
                        borrador.id, codigoActivo, proyecto, kilometraje, horometro,
                        tipoIntervencion, posicion, huella, marcaLlanta, medidaLlanta,
                        serieLlanta, motivo, observaciones, nombreTecnico
                    )
                },
                enabled = !guardando &&
                        tipoIntervencion.isNotBlank() &&
                        posicion.isNotBlank() &&
                        nombreTecnico.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar intervención")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}