package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class RegistroRevisionIntervencion(
    val id: String,
    val codigoActivo: String,
    val proyecto: String,
    val kilometraje: Double?,
    val horometro: Double?,
    val tipoIntervencion: String,
    val posicion: String,
    val huella: Double?,
    val marcaLlanta: String,
    val medidaLlanta: String,
    val serieLlanta: String,
    val motivo: String,
    val observaciones: String,
    val nombreTecnico: String,
    val uidUsuario: String,
    val estadoRegistro: String
)

@Composable
fun RevisionIntervencionScreen(
    registros: List<RegistroRevisionIntervencion>,
    cargando: Boolean,
    mensaje: String,
    onAprobar: (RegistroRevisionIntervencion) -> Unit,
    onDevolver: (RegistroRevisionIntervencion, String) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Revisión de intervenciones",
                style = MaterialTheme.typography.headlineSmall
            )
            if (mensaje.isNotBlank()) Text(mensaje)
            if (cargando) Text("Cargando intervenciones...")
            Text("Pendientes: ${registros.size}")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(registros, key = { it.id }) { registro ->
                var motivoDevolucion by remember(registro.id) {
                    mutableStateOf("")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(registro.codigoActivo, style = MaterialTheme.typography.titleMedium)
                        Text("Intervención: ${registro.tipoIntervencion}")
                        Text("Posición: ${registro.posicion}")
                        Text("Huella: ${registro.huella?.let { "$it mm" } ?: "Sin registrar"}")
                        Text("Marca: ${registro.marcaLlanta.ifBlank { "Sin registrar" }}")
                        Text("Medida: ${registro.medidaLlanta.ifBlank { "Sin registrar" }}")
                        Text("Serie: ${registro.serieLlanta.ifBlank { "Sin registrar" }}")
                        Text("Motivo: ${registro.motivo.ifBlank { "Sin registrar" }}")
                        Text("Técnico: ${registro.nombreTecnico.ifBlank { "Sin registrar" }}")

                        OutlinedTextField(
                            value = motivoDevolucion,
                            onValueChange = { motivoDevolucion = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Motivo de devolución") },
                            minLines = 2
                        )

                        Button(
                            onClick = { onAprobar(registro) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aprobar intervención")
                        }

                        OutlinedButton(
                            onClick = {
                                onDevolver(registro, motivoDevolucion)
                            },
                            enabled = motivoDevolucion.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Devolver para corrección")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Text("Volver al menú principal")
        }
    }
}