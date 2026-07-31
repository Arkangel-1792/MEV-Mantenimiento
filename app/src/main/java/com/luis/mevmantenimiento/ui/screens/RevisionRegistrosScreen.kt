package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class RegistroRevision(
    val id: String,
    val codigoActivo: String,
    val tipoServicio: String,
    val kilometraje: Double?,
    val horometro: Double?,
    val accionEjecutada: String,
    val observaciones: String,
    val ordenTrabajo: String,
    val numeroPedido: String,
    val uidUsuario: String,
    val estadoRegistro: String
)

@Composable
fun RevisionRegistrosScreen(
    registros: List<RegistroRevision>,
    cargando: Boolean,
    mensaje: String,
    onAprobar: (RegistroRevision) -> Unit,
    onDevolver: (
        registro: RegistroRevision,
        motivoDevolucion: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    var registroSeleccionado by remember {
        mutableStateOf<RegistroRevision?>(null)
    }

    var motivoDevolucion by remember {
        mutableStateOf("")
    }

    var errorMotivo by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Revisión de registros",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta, aprueba o devuelve los registros enviados.",
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
                    text = "Procesando información...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Registros pendientes: ${registros.size}",
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
                    text = "No existen registros pendientes de revisión.",
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
                    RegistroRevisionCard(
                        registro = registro,
                        habilitado = !cargando,
                        onAprobar = {
                            onAprobar(registro)
                        },
                        onSolicitarDevolucion = {
                            registroSeleccionado = registro
                            motivoDevolucion = ""
                            errorMotivo = ""
                        }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            enabled = !cargando,
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

    if (registroSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    registroSeleccionado = null
                    motivoDevolucion = ""
                    errorMotivo = ""
                }
            },
            title = {
                Text("Devolver registro")
            },
            text = {
                Column {
                    Text(
                        text = "Indica qué debe corregirse en el registro " +
                                "${registroSeleccionado?.codigoActivo.orEmpty()}."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = motivoDevolucion,
                        onValueChange = {
                            motivoDevolucion = it

                            if (it.isNotBlank()) {
                                errorMotivo = ""
                            }
                        },
                        label = {
                            Text("Motivo de devolución")
                        },
                        placeholder = {
                            Text(
                                "Ejemplo: corregir el horómetro " +
                                        "y ampliar la observación."
                            )
                        },
                        supportingText = {
                            if (errorMotivo.isNotBlank()) {
                                Text(errorMotivo)
                            }
                        },
                        isError = errorMotivo.isNotBlank(),
                        enabled = !cargando,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val registro = registroSeleccionado
                        val motivo = motivoDevolucion.trim()

                        if (motivo.isBlank()) {
                            errorMotivo =
                                "Debes escribir el motivo de devolución."
                            return@Button
                        }

                        if (registro != null) {
                            onDevolver(
                                registro,
                                motivo
                            )

                            registroSeleccionado = null
                            motivoDevolucion = ""
                            errorMotivo = ""
                        }
                    },
                    enabled = !cargando
                ) {
                    Text("Confirmar devolución")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        registroSeleccionado = null
                        motivoDevolucion = ""
                        errorMotivo = ""
                    },
                    enabled = !cargando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun RegistroRevisionCard(
    registro: RegistroRevision,
    habilitado: Boolean,
    onAprobar: () -> Unit,
    onSolicitarDevolucion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = registro.codigoActivo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Servicio: ${registro.tipoServicio}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = obtenerLecturaRevision(registro),
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
                    text =
                        "Orden de trabajo: ${registro.ordenTrabajo}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (registro.numeroPedido.isNotBlank()) {
                Text(
                    text = "Pedido: ${registro.numeroPedido}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Usuario: ${registro.uidUsuario}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSolicitarDevolucion,
                    enabled = habilitado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Devolver")
                }

                Button(
                    onClick = onAprobar,
                    enabled = habilitado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aprobar")
                }
            }
        }
    }
}

private fun obtenerLecturaRevision(
    registro: RegistroRevision
): String {
    val kilometraje = registro.kilometraje?.let {
        "$it km"
    } ?: "Sin registro"

    val horometro = registro.horometro?.let {
        "$it h"
    } ?: "Sin registro"

    return "Kilometraje: $kilometraje · Horómetro: $horometro"
}