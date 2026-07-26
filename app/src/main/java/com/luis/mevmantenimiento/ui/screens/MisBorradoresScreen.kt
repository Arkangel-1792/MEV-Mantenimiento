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
    val estadoRegistro: String
)

@Composable
fun MisBorradoresScreen(
    borradores: List<BorradorMantenimiento>,
    cargando: Boolean,
    mensaje: String,
    onSeleccionarBorrador: (BorradorMantenimiento) -> Unit,
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
                text = "Mis borradores",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Registros guardados que todavía pueden editarse y enviarse.",
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
                    text = "Cargando borradores...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Borradores encontrados: ${borradores.size}",
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
                    text = "No tienes borradores pendientes.",
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
                    key = { borrador ->
                        borrador.id
                    }
                ) { borrador ->
                    BorradorCard(
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
private fun BorradorCard(
    borrador: BorradorMantenimiento,
    onEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = borrador.codigoActivo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Servicio: ${borrador.tipoServicio}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = obtenerLecturaBorrador(borrador),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (borrador.accionEjecutada.isBlank()) {
                    "Acción: Sin registrar"
                } else {
                    "Acción: ${borrador.accionEjecutada}"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar borrador")
            }
        }
    }
}

private fun obtenerLecturaBorrador(
    borrador: BorradorMantenimiento
): String {
    val kilometraje = borrador.kilometraje?.let {
        "$it km"
    } ?: "Sin registro"

    val horometro = borrador.horometro?.let {
        "$it h"
    } ?: "Sin registro"

    return "Kilometraje: $kilometraje · Horómetro: $horometro"
}