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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class BorradorHuella(
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
    val motivoDevolucion: String = ""
)

@Composable
fun MisBorradoresHuellaScreen(
    borradores: List<BorradorHuella>,
    cargando: Boolean,
    mensaje: String,
    onSeleccionarBorrador: (BorradorHuella) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Borradores de toma de huella",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registros guardados pendientes de completar o enviar.",
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

                    Text("Cargando borradores...")
                }
            }

            borradores.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No existen borradores de toma de huella.",
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
                        items = borradores,
                        key = { borrador ->
                            borrador.id
                        }
                    ) { borrador ->
                        TarjetaBorradorHuella(
                            borrador = borrador,
                            onSeleccionar = {
                                onSeleccionarBorrador(borrador)
                            }
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
private fun TarjetaBorradorHuella(
    borrador: BorradorHuella,
    onSeleccionar: () -> Unit
) {
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
            Text(
                text = borrador.codigoActivo.ifBlank {
                    "Activo sin identificar"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (borrador.proyecto.isNotBlank()) {
                Text(
                    text = "Proyecto: ${borrador.proyecto}"
                )
            }

            borrador.kilometraje?.let { kilometraje ->
                Text(
                    text = "Kilometraje: ${formatearNumero(kilometraje)} km"
                )
            }

            borrador.horometro?.let { horometro ->
                Text(
                    text = "Horómetro: ${formatearNumero(horometro)} h"
                )
            }

            Text(
                text = "Mediciones registradas: ${
                    borrador.huellas.count { huella ->
                        huella != null
                    }
                }"
            )

            if (borrador.estadoGeneral.isNotBlank()) {
                Text(
                    text = "Estado general: ${borrador.estadoGeneral}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onSeleccionar
                ) {
                    Text("Abrir borrador")
                }
            }
        }
    }
}

private fun formatearNumero(
    valor: Double
): String {
    return if (valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString()
    }
}