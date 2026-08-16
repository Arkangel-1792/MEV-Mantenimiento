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
    registrosMantenimiento: List<RegistroHistorial>,
    registrosHuella: List<RegistroHistorialHuella>,
    registrosIntervencion: List<RegistroHistorialIntervencion>,
    cargando: Boolean,
    mensaje: String,
    onVolver: () -> Unit
) {
    val total =
        registrosMantenimiento.size +
                registrosHuella.size +
                registrosIntervencion.size

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
                text = "Consulta mantenimientos, tomas de huella e intervenciones enviadas.",
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
                Text("Cargando historial...")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Registros encontrados: $total",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!cargando && total == 0) {
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
                if (registrosMantenimiento.isNotEmpty()) {
                    item {
                        Text(
                            text = "Mantenimiento",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(
                        items = registrosMantenimiento,
                        key = { "M_${it.id}" }
                    ) { registro ->
                        TarjetaMantenimiento(registro)
                    }
                }

                if (registrosHuella.isNotEmpty()) {
                    item {
                        Text(
                            text = "Toma de huella",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(
                        items = registrosHuella,
                        key = { "H_${it.id}" }
                    ) { registro ->
                        TarjetaHuella(registro)
                    }
                }

                if (registrosIntervencion.isNotEmpty()) {
                    item {
                        Text(
                            text = "Intervenciones de llanta",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(
                        items = registrosIntervencion,
                        key = { "I_${it.id}" }
                    ) { registro ->
                        TarjetaIntervencion(registro)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Volver al menú principal")
        }
    }
}

@Composable
private fun TarjetaMantenimiento(
    registro: RegistroHistorial
) {
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

            EstadoHistorial(registro.estadoRegistro)

            Text("Servicio: ${registro.tipoServicio}")
            Text(
                "Kilometraje: ${registro.kilometraje ?: "Sin registro"} · " +
                        "Horómetro: ${registro.horometro ?: "Sin registro"}"
            )

            if (registro.accionEjecutada.isNotBlank()) {
                Text("Acción: ${registro.accionEjecutada}")
            }

            if (registro.observaciones.isNotBlank()) {
                Text("Observaciones: ${registro.observaciones}")
            }

            if (registro.ordenTrabajo.isNotBlank()) {
                Text("Orden de trabajo: ${registro.ordenTrabajo}")
            }

            if (registro.numeroPedido.isNotBlank()) {
                Text("Pedido: ${registro.numeroPedido}")
            }

            MotivoDevuelto(
                estado = registro.estadoRegistro,
                motivo = registro.motivoDevolucion
            )
        }
    }
}

@Composable
private fun TarjetaHuella(
    registro: RegistroHistorialHuella
) {
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

            EstadoHistorial(registro.estadoRegistro)

            if (registro.proyecto.isNotBlank()) {
                Text("Proyecto: ${registro.proyecto}")
            }

            Text(
                "Kilometraje: ${registro.kilometraje ?: "Sin registro"} · " +
                        "Horómetro: ${registro.horometro ?: "Sin registro"}"
            )

            Text(
                "Mediciones registradas: ${
                    registro.huellas.count { it != null }
                }"
            )

            if (registro.estadoGeneral.isNotBlank()) {
                Text("Estado general: ${registro.estadoGeneral}")
            }

            if (registro.novedad.isNotBlank()) {
                Text("Novedad: ${registro.novedad}")
            }

            if (registro.nombreTecnico.isNotBlank()) {
                Text("Técnico: ${registro.nombreTecnico}")
            }

            MotivoDevuelto(
                estado = registro.estadoRegistro,
                motivo = registro.motivoDevolucion
            )
        }
    }
}

@Composable
private fun TarjetaIntervencion(
    registro: RegistroHistorialIntervencion
) {
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

            EstadoHistorial(registro.estadoRegistro)

            Text("Intervención: ${registro.tipoIntervencion}")
            Text("Posición: ${registro.posicion.ifBlank { "Sin registrar" }}")
            Text(
                "Huella: ${
                    registro.huella?.let { "$it mm" } ?: "Sin registrar"
                }"
            )

            if (registro.marcaLlanta.isNotBlank()) {
                Text("Marca: ${registro.marcaLlanta}")
            }

            if (registro.medidaLlanta.isNotBlank()) {
                Text("Medida: ${registro.medidaLlanta}")
            }

            if (registro.serieLlanta.isNotBlank()) {
                Text("Serie: ${registro.serieLlanta}")
            }

            if (registro.motivo.isNotBlank()) {
                Text("Motivo: ${registro.motivo}")
            }

            if (registro.observaciones.isNotBlank()) {
                Text("Observaciones: ${registro.observaciones}")
            }

            if (registro.nombreTecnico.isNotBlank()) {
                Text("Técnico: ${registro.nombreTecnico}")
            }

            MotivoDevuelto(
                estado = registro.estadoRegistro,
                motivo = registro.motivoDevolucion
            )
        }
    }
}

@Composable
private fun EstadoHistorial(
    estado: String
) {
    Text(
        text = "Estado: $estado",
        style = MaterialTheme.typography.labelLarge,
        color = when (estado.uppercase()) {
            "APROBADO" -> Color(0xFF2E7D32)
            "DEVUELTO" -> MaterialTheme.colorScheme.error
            "ENVIADO" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }
    )
}

@Composable
private fun MotivoDevuelto(
    estado: String,
    motivo: String
) {
    if (
        estado.uppercase() == "DEVUELTO" &&
        motivo.isNotBlank()
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Motivo de devolución:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = motivo,
            color = MaterialTheme.colorScheme.error
        )
    }
}