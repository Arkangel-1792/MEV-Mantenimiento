package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class RegistroHistorialIntervencion(
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
    val estadoRegistro: String,
    val motivoDevolucion: String = ""
)

@Composable
fun HistorialIntervencionScreen(
    registros: List<RegistroHistorialIntervencion>,
    cargando: Boolean,
    mensaje: String,
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
                text = "Historial de intervenciones",
                style = MaterialTheme.typography.headlineSmall
            )
            if (mensaje.isNotBlank()) Text(mensaje)
            if (cargando) Text("Cargando historial...")
            Text("Registros encontrados: ${registros.size}")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(registros, key = { it.id }) { registro ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(registro.codigoActivo, style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${registro.estadoRegistro}")
                        Text("Intervención: ${registro.tipoIntervencion}")
                        Text("Posición: ${registro.posicion}")
                        Text("Huella: ${registro.huella?.let { "$it mm" } ?: "Sin registrar"}")
                        Text("Marca: ${registro.marcaLlanta.ifBlank { "Sin registrar" }}")
                        Text("Medida: ${registro.medidaLlanta.ifBlank { "Sin registrar" }}")
                        Text("Serie: ${registro.serieLlanta.ifBlank { "Sin registrar" }}")
                        Text("Técnico: ${registro.nombreTecnico.ifBlank { "Sin registrar" }}")

                        if (registro.motivo.isNotBlank()) {
                            Text("Motivo: ${registro.motivo}")
                        }

                        if (registro.observaciones.isNotBlank()) {
                            Text("Observaciones: ${registro.observaciones}")
                        }

                        if (registro.estadoRegistro == "DEVUELTO") {
                            Text(
                                text = "Motivo de devolución: ${registro.motivoDevolucion.ifBlank { "Sin detalle" }}",
                                color = MaterialTheme.colorScheme.error
                            )
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