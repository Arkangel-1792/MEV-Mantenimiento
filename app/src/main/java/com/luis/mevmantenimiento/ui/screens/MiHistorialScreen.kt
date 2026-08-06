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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class RegistroHistorial(
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
fun MiHistorialScreen(
    registros: List<RegistroHistorial>,
    cargando: Boolean,
    mensaje: String,
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
                text = "Mi historial",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta el estado de los registros que has enviado.",
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
                    text = "Cargando historial...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Registros encontrados: ${registros.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!cargando && registros.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No existen registros en tu historial.",
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
                    items = registros,
                    key = { registro ->
                        registro.id
                    }
                ) { registro ->
                    RegistroHistorialCard(registro)
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
private fun RegistroHistorialCard(
    registro: RegistroHistorial
) {
    val colorEstado = obtenerColorEstado(registro.estadoRegistro)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = registro.codigoActivo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Estado: ${registro.estadoRegistro}",
                style = MaterialTheme.typography.labelLarge,
                color = colorEstado
            )

            Text(
                text = "Servicio: ${registro.tipoServicio}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = obtenerLecturaHistorial(registro),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (registro.accionEjecutada.isBlank()) {
                    "Acción: Sin registrar"
                } else {
                    "Acción: ${registro.accionEjecutada}"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (registro.observaciones.isNotBlank()) {
                Text(
                    text = "Observaciones: ${registro.observaciones}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (registro.ordenTrabajo.isNotBlank()) {
                Text(
                    text = "Orden de trabajo: ${registro.ordenTrabajo}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (registro.numeroPedido.isNotBlank()) {
                Text(
                    text = "Pedido: ${registro.numeroPedido}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (
                registro.estadoRegistro == "DEVUELTO" &&
                registro.motivoDevolucion.isNotBlank()
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Motivo de devolución:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = registro.motivoDevolucion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun obtenerColorEstado(
    estado: String
): Color {
    return when (estado) {
        "APROBADO" -> Color(0xFF2E7D32)
        "DEVUELTO" -> MaterialTheme.colorScheme.error
        "ENVIADO" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
}

private fun obtenerLecturaHistorial(
    registro: RegistroHistorial
): String {
    val kilometraje = registro.kilometraje?.let {
        "$it km"
    } ?: "Sin registro"

    val horometro = registro.horometro?.let {
        "$it h"
    } ?: "Sin registro"

    return "Kilometraje: $kilometraje · Horómetro: $horometro"
}