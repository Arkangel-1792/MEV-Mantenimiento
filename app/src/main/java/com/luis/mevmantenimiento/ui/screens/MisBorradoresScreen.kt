package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class BorradorMantenimiento(
    val id: String,
    val codigoActivo: String,
    val tipoServicio: String,
    val kilometraje: Double?,
    val horometro: Double?,
    val accionEjecutada: String,
    val observaciones: String,
    val ordenTrabajo: String,
    val numeroPedido: String,
    val estadoRegistro: String,
    val motivoDevolucion: String = ""
)

@Composable
fun MisBorradoresScreen(
    borradoresMantenimiento: List<BorradorMantenimiento>,
    borradoresHuella: List<BorradorHuella>,
    borradoresIntervencion: List<BorradorIntervencionLlanta>,
    cargando: Boolean,
    mensaje: String,
    onSeleccionarMantenimiento: (BorradorMantenimiento) -> Unit,
    onSeleccionarHuella: (BorradorHuella) -> Unit,
    onSeleccionarIntervencion: (BorradorIntervencionLlanta) -> Unit,
    onVolver: () -> Unit
) {
    val total = borradoresMantenimiento.size + borradoresHuella.size + borradoresIntervencion.size

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Mis borradores y correcciones", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Mantenimiento, tomas de huella e intervenciones pendientes en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(mensaje)
            }
            if (cargando) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Cargando registros...")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Registros encontrados: $total")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!cargando && total == 0) {
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No tienes borradores ni registros devueltos pendientes.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (borradoresMantenimiento.isNotEmpty()) {
                    item { Text("Mantenimiento", style = MaterialTheme.typography.titleLarge) }
                    items(borradoresMantenimiento, key = { "M_${it.id}" }) { borrador ->
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
                                val devuelto = borrador.estadoRegistro == "DEVUELTO"
                                Text(borrador.codigoActivo, style = MaterialTheme.typography.titleMedium)
                                Text(if (devuelto) "Estado: DEVUELTO PARA CORRECCIÓN" else "Estado: BORRADOR")
                                Text("Servicio: ${borrador.tipoServicio}")
                                Text("Kilometraje: ${borrador.kilometraje ?: "Sin registro"} · Horómetro: ${borrador.horometro ?: "Sin registro"}")
                                if (devuelto) {
                                    Text(
                                        "Motivo: ${borrador.motivoDevolucion.ifBlank { "Sin detalle" }}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Button(
                                    onClick = { onSeleccionarMantenimiento(borrador) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (devuelto) "Corregir registro" else "Editar borrador")
                                }
                            }
                        }
                    }
                }

                if (borradoresHuella.isNotEmpty()) {
                    item { Text("Toma de huella", style = MaterialTheme.typography.titleLarge) }
                    items(borradoresHuella, key = { "H_${it.id}" }) { borrador ->
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
                                val devuelto = borrador.estadoRegistro == "DEVUELTO"
                                Text(borrador.codigoActivo, style = MaterialTheme.typography.titleMedium)
                                Text(if (devuelto) "Estado: DEVUELTO PARA CORRECCIÓN" else "Estado: BORRADOR")
                                Text("Proyecto: ${borrador.proyecto.ifBlank { "Sin registrar" }}")
                                Text("Estado general: ${borrador.estadoGeneral.ifBlank { "Sin registrar" }}")
                                if (devuelto) {
                                    Text(
                                        "Motivo: ${borrador.motivoDevolucion.ifBlank { "Sin detalle" }}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Button(
                                    onClick = { onSeleccionarHuella(borrador) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (devuelto) "Corregir registro" else "Editar borrador")
                                }
                            }
                        }
                    }
                }

                if (borradoresIntervencion.isNotEmpty()) {
                    item { Text("Intervenciones de llanta", style = MaterialTheme.typography.titleLarge) }
                    items(borradoresIntervencion, key = { "I_${it.id}" }) { borrador ->
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
                                val devuelto = borrador.estadoRegistro == "DEVUELTO"
                                Text(borrador.codigoActivo, style = MaterialTheme.typography.titleMedium)
                                Text(if (devuelto) "Estado: DEVUELTO PARA CORRECCIÓN" else "Estado: BORRADOR")
                                Text("Intervención: ${borrador.tipoIntervencion.ifBlank { "Sin registrar" }}")
                                Text("Posición: ${borrador.posicion.ifBlank { "Sin registrar" }}")
                                Text("Huella: ${borrador.huella?.let { "$it mm" } ?: "Sin registrar"}")
                                if (devuelto) {
                                    Text(
                                        "Motivo: ${borrador.motivoDevolucion.ifBlank { "Sin detalle" }}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Button(
                                    onClick = { onSeleccionarIntervencion(borrador) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (devuelto) "Corregir registro" else "Editar borrador")
                                }
                            }
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