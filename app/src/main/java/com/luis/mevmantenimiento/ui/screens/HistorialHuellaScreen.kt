package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class RegistroHistorialHuella(
    val id: String,
    val codigoActivo: String,
    val proyecto: String,
    val kilometraje: Double?,
    val horometro: Double?,
    val huellas: List<Double?>,
    val estadoGeneral: String,
    val novedad: String,
    val nombreTecnico: String,
    val estadoRegistro: String,
    val motivoDevolucion: String
)

@Composable
fun HistorialHuellaScreen(
    registros: List<RegistroHistorialHuella>,
    cargando: Boolean,
    mensaje: String,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Historial de tomas de huella",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registros enviados, aprobados o devueltos.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            cargando -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Cargando historial...")
                }
            }

            registros.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No existen tomas de huella en el historial.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = registros,
                        key = { registro ->
                            registro.id
                        }
                    ) { registro ->
                        TarjetaHistorialHuella(
                            registro = registro
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}

@Composable
private fun TarjetaHistorialHuella(
    registro: RegistroHistorialHuella
) {
    val colorEstado = obtenerColorEstadoHuella(
        registro.estadoRegistro
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = registro.codigoActivo.ifBlank {
                        "Activo sin identificar"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = registro.estadoRegistro,
                    color = colorEstado,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (registro.proyecto.isNotBlank()) {
                Text(
                    text = "Proyecto: ${registro.proyecto}"
                )
            }

            registro.kilometraje?.let { kilometraje ->
                Text(
                    text = "Kilometraje: ${
                        formatearNumeroHistorial(kilometraje)
                    } km"
                )
            }

            registro.horometro?.let { horometro ->
                Text(
                    text = "Horómetro: ${
                        formatearNumeroHistorial(horometro)
                    } h"
                )
            }

            Text(
                text = "Mediciones registradas: ${
                    registro.huellas.count { huella ->
                        huella != null
                    }
                }"
            )

            if (registro.estadoGeneral.isNotBlank()) {
                Text(
                    text =
                        "Estado general: ${registro.estadoGeneral}"
                )
            }

            if (registro.novedad.isNotBlank()) {
                Text(
                    text = "Novedad: ${registro.novedad}"
                )
            }

            if (registro.nombreTecnico.isNotBlank()) {
                Text(
                    text =
                        "Técnico: ${registro.nombreTecnico}"
                )
            }

            if (
                registro.estadoRegistro == "DEVUELTO" &&
                registro.motivoDevolucion.isNotBlank()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Motivo de devolución:",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = registro.motivoDevolucion,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun obtenerColorEstadoHuella(
    estado: String
): Color {
    return when (estado.uppercase()) {
        "APROBADO" -> Color(0xFF2E7D32)
        "DEVUELTO" -> Color(0xFFC62828)
        "ENVIADO" -> Color(0xFF1565C0)
        else -> Color.Gray
    }
}

private fun formatearNumeroHistorial(
    valor: Double
): String {
    return if (valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString()
    }
}