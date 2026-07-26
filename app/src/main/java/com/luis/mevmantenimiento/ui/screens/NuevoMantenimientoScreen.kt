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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun NuevoMantenimientoScreen(
    activos: List<ActivoResumen>,
    guardandoMantenimiento: Boolean,
    mensajeMantenimiento: String,
    onGuardarBorrador: (
        codigoActivo: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String
    ) -> Unit,
    onEnviar: (
        codigoActivo: String,
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
    var codigoActivo by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("PREVENTIVO") }
    var kilometraje by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    var accionEjecutada by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var ordenTrabajo by remember { mutableStateOf("") }
    var numeroPedido by remember { mutableStateOf("") }

    val activoEncontrado = activos.firstOrNull {
        it.codigo.equals(codigoActivo.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nuevo mantenimiento",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Registra una actividad preventiva o correctiva.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (mensajeMantenimiento.isNotBlank()) {
            Text(
                text = mensajeMantenimiento,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (guardandoMantenimiento) {
            Text(
                text = "No cierres la aplicación mientras se guarda el registro.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = codigoActivo,
            onValueChange = {
                codigoActivo = it.uppercase()
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Código del activo")
            },
            supportingText = {
                when {
                    codigoActivo.isBlank() -> {
                        Text("Ejemplo: VVOLQ0099")
                    }

                    activoEncontrado != null -> {
                        Text(
                            "${activoEncontrado.subtipo} · " +
                                    "${activoEncontrado.marca} " +
                                    activoEncontrado.modelo
                        )
                    }

                    else -> {
                        Text("El código no consta en el catálogo.")
                    }
                }
            },
            singleLine = true
        )

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
                onGuardarBorrador(
                    codigoActivo,
                    tipoServicio,
                    kilometraje,
                    horometro,
                    accionEjecutada,
                    observaciones,
                    ordenTrabajo,
                    numeroPedido
                )
            },
            enabled = activoEncontrado != null &&
                    !guardandoMantenimiento,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar borrador")
        }

        Button(
            onClick = {
                onEnviar(
                    codigoActivo,
                    tipoServicio,
                    kilometraje,
                    horometro,
                    accionEjecutada,
                    observaciones,
                    ordenTrabajo,
                    numeroPedido
                )
            },
            enabled = activoEncontrado != null &&
                    accionEjecutada.isNotBlank() &&
                    !guardandoMantenimiento,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar registro")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú principal")
        }
    }
}