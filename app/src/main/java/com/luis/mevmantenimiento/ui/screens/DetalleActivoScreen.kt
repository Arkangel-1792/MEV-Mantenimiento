package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun DetalleActivoScreen(
    activo: ActivoResumen,
    onEditar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Detalle del activo",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = activo.codigo,
            style = MaterialTheme.typography.titleLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampoDetalle("Subtipo", activo.subtipo)
                CampoDetalle("Tipo", activo.tipo)
                CampoDetalle("Marca", activo.marca)
                CampoDetalle("Modelo", activo.modelo)
                CampoDetalle("Indicador", activo.indicador)
                CampoDetalle("Ubicación actual", activo.ubicacionActual)
                CampoDetalle("Estado", activo.status)

                CampoDetalle(
                    "Horómetro",
                    activo.horometro?.let { "$it h" } ?: "Sin registro"
                )

                CampoDetalle(
                    "Kilometraje",
                    activo.kilometraje?.let { "$it km" } ?: "Sin registro"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onEditar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar activo")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun CampoDetalle(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = valor.ifBlank { "Sin registro" },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}