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

@Composable
fun MisBorradoresIntervencionScreen(
    borradores: List<BorradorIntervencionLlanta>,
    cargando: Boolean,
    mensaje: String,
    onSeleccionarBorrador: (BorradorIntervencionLlanta) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Borradores de intervención",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Intervenciones guardadas o devueltas que todavía pueden corregirse y enviarse.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (cargando) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cargando intervenciones...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Registros encontrados: ${borradores.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!cargando && borradores.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No tienes borradores ni intervenciones devueltas pendientes.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = borradores,
                    key = { borrador -> borrador.id }
                ) { borrador ->
                    BorradorIntervencionCard(
                        borrador = borrador,
                        onEditar = {
                            onSeleccionarBorrador(borrador)
                        }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 16.dp
                )
        ) {
            Text("Volver al menú principal")
        }
    }
}

@Composable
private fun BorradorIntervencionCard(
    borrador: BorradorIntervencionLlanta,
    onEditar: () -> Unit
) {
    val esDevuelto =
        borrador.estadoRegistro == "DEVUELTO"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = borrador.codigoActivo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = if (esDevuelto) {
                    "Estado: DEVUELTO PARA CORRECCIÓN"
                } else {
                    "Estado: BORRADOR"
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (esDevuelto) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Text(
                text =
                    "Intervención: ${borrador.tipoIntervencion.ifBlank { "Sin registrar" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text =
                    "Posición: ${borrador.posicion.ifBlank { "Sin registrar" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text =
                    "Huella: ${borrador.huella?.let { "$it mm" } ?: "Sin registrar"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = obtenerLecturaIntervencion(borrador),
                style = MaterialTheme.typography.bodyMedium
            )

            if (borrador.marcaLlanta.isNotBlank()) {
                Text(
                    text = "Marca: ${borrador.marcaLlanta}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (borrador.medidaLlanta.isNotBlank()) {
                Text(
                    text = "Medida: ${borrador.medidaLlanta}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (borrador.serieLlanta.isNotBlank()) {
                Text(
                    text = "Serie: ${borrador.serieLlanta}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (borrador.motivo.isNotBlank()) {
                Text(
                    text = "Motivo: ${borrador.motivo}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (esDevuelto) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Motivo de devolución:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text =
                        borrador.motivoDevolucion.ifBlank {
                            "El revisor no registró un motivo."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (esDevuelto) {
                        "Corregir intervención"
                    } else {
                        "Editar borrador"
                    }
                )
            }
        }
    }
}

private fun obtenerLecturaIntervencion(
    borrador: BorradorIntervencionLlanta
): String {
    val kilometraje =
        borrador.kilometraje?.let {
            "$it km"
        } ?: "Sin registro"

    val horometro =
        borrador.horometro?.let {
            "$it h"
        } ?: "Sin registro"

    return "Kilometraje: $kilometraje · Horómetro: $horometro"
}