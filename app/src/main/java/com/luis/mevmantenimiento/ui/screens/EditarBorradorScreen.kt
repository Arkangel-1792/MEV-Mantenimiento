package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditarBorradorScreen(
    borrador: BorradorMantenimiento,
    guardando: Boolean,
    mensaje: String,
    onActualizarBorrador: (
        id: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String
    ) -> Unit,
    onEnviarBorrador: (
        id: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var tipoServicio by remember(borrador.id) {
        mutableStateOf(borrador.tipoServicio)
    }

    var kilometraje by remember(borrador.id) {
        mutableStateOf(
            borrador.kilometraje
                ?.toString()
                .orEmpty()
        )
    }

    var horometro by remember(borrador.id) {
        mutableStateOf(
            borrador.horometro
                ?.toString()
                .orEmpty()
        )
    }

    var accionEjecutada by remember(borrador.id) {
        mutableStateOf(borrador.accionEjecutada)
    }

    var observaciones by remember(borrador.id) {
        mutableStateOf(borrador.observaciones)
    }

    var ordenTrabajo by remember(borrador.id) {
        mutableStateOf(borrador.ordenTrabajo)
    }

    var numeroPedido by remember(borrador.id) {
        mutableStateOf(borrador.numeroPedido)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Editar borrador",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Activo: ${borrador.codigoActivo}",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Puedes modificar el registro antes de enviarlo.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (guardando) {
            Text(
                text = "No cierres la aplicación mientras se guarda.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "Tipo de servicio",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = tipoServicio == "PREVENTIVO",
                onClick = {
                    tipoServicio = "PREVENTIVO"
                },
                label = {
                    Text("Preventivo")
                }
            )

            FilterChip(
                selected = tipoServicio == "CORRECTIVO",
                onClick = {
                    tipoServicio = "CORRECTIVO"
                },
                label = {
                    Text("Correctivo")
                }
            )
        }

        OutlinedTextField(
            value = kilometraje,
            onValueChange = {
                kilometraje = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Kilometraje")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = horometro,
            onValueChange = {
                horometro = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Horómetro")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = accionEjecutada,
            onValueChange = {
                accionEjecutada = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    if (tipoServicio == "PREVENTIVO") {
                        "Mantenimiento realizado"
                    } else {
                        "Reparación o acción ejecutada"
                    }
                )
            },
            minLines = 3
        )

        OutlinedTextField(
            value = observaciones,
            onValueChange = {
                observaciones = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Observaciones")
            },
            minLines = 3
        )

        OutlinedTextField(
            value = ordenTrabajo,
            onValueChange = {
                ordenTrabajo = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Orden de trabajo")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = numeroPedido,
            onValueChange = {
                numeroPedido = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Número de pedido")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onActualizarBorrador(
                    borrador.id,
                    tipoServicio,
                    kilometraje,
                    horometro,
                    accionEjecutada,
                    observaciones,
                    ordenTrabajo,
                    numeroPedido
                )
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }

        Button(
            onClick = {
                onEnviarBorrador(
                    borrador.id,
                    tipoServicio,
                    kilometraje,
                    horometro,
                    accionEjecutada,
                    observaciones,
                    ordenTrabajo,
                    numeroPedido
                )
            },
            enabled = !guardando &&
                    accionEjecutada.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar borrador")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a mis borradores")
        }
    }
}