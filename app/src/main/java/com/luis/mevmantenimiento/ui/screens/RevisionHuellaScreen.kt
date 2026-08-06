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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class RegistroRevisionHuella(
    val id: String,
    val codigoActivo: String,
    val proyecto: String,
    val kilometraje: Double?,
    val horometro: Double?,
    val huellas: List<Double?>,
    val estadoGeneral: String,
    val novedad: String,
    val nombreTecnico: String,
    val uidUsuario: String,
    val estadoRegistro: String
)

@Composable
fun RevisionHuellaScreen(
    registros: List<RegistroRevisionHuella>,
    cargando: Boolean,
    mensaje: String,
    onAprobar: (RegistroRevisionHuella) -> Unit,
    onDevolver: (
        registro: RegistroRevisionHuella,
        motivoDevolucion: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Revisión de tomas de huella",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registros enviados pendientes de aprobación.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = if (
                    mensaje.contains(
                        "correctamente",
                        ignoreCase = true
                    )
                ) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
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

                    Text("Cargando tomas de huella...")
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
                        text = "No existen tomas pendientes de revisión.",
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
                        TarjetaRevisionHuella(
                            registro = registro,
                            accionesHabilitadas = !cargando,
                            onAprobar = {
                                onAprobar(registro)
                            },
                            onDevolver = { motivo ->
                                onDevolver(
                                    registro,
                                    motivo
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text("Volver al menú")
        }
    }
}

@Composable
private fun TarjetaRevisionHuella(
    registro: RegistroRevisionHuella,
    accionesHabilitadas: Boolean,
    onAprobar: () -> Unit,
    onDevolver: (String) -> Unit
) {
    var mostrarDevolucion by remember {
        mutableStateOf(false)
    }

    var motivoDevolucion by remember {
        mutableStateOf("")
    }

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
                    color = MaterialTheme.colorScheme.primary,
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
                        formatearNumeroRevisionHuella(
                            kilometraje
                        )
                    } km"
                )
            }

            registro.horometro?.let { horometro ->
                Text(
                    text = "Horómetro: ${
                        formatearNumeroRevisionHuella(
                            horometro
                        )
                    } h"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Profundidades registradas:",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            registro.huellas.forEachIndexed { indice, huella ->
                if (huella != null) {
                    Text(
                        text = "P${indice + 1}: ${
                            formatearNumeroRevisionHuella(
                                huella
                            )
                        } mm"
                    )
                }
            }

            if (registro.estadoGeneral.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAprobar,
                modifier = Modifier.fillMaxWidth(),
                enabled = accionesHabilitadas
            ) {
                Text("Aprobar toma de huella")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    mostrarDevolucion =
                        !mostrarDevolucion
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = accionesHabilitadas
            ) {
                Text(
                    text = if (mostrarDevolucion) {
                        "Cancelar devolución"
                    } else {
                        "Devolver para corrección"
                    }
                )
            }

            if (mostrarDevolucion) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = motivoDevolucion,
                    onValueChange = {
                        motivoDevolucion = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Motivo de devolución")
                    },
                    minLines = 2,
                    enabled = accionesHabilitadas
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onDevolver(
                            motivoDevolucion.trim()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled =
                        accionesHabilitadas &&
                                motivoDevolucion.isNotBlank()
                ) {
                    Text("Confirmar devolución")
                }
            }
        }
    }
}

private fun formatearNumeroRevisionHuella(
    valor: Double
): String {
    return if (valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString()
    }
}
