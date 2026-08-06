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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun EditarBorradorHuellaScreen(
    borrador: BorradorHuella,
    activos: List<ActivoResumen>,
    guardando: Boolean,
    mensaje: String,
    onActualizarBorrador: (
        idRegistro: String,
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String
    ) -> Unit,
    onEnviarBorrador: (
        idRegistro: String,
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String
    ) -> Unit,
    onVolver: () -> Unit
) {
    val activoSeleccionado = remember(
        borrador.codigoActivo,
        activos
    ) {
        activos.firstOrNull {
            it.codigo.equals(
                borrador.codigoActivo,
                ignoreCase = true
            )
        }
    }

    val cantidadPosiciones = remember(activoSeleccionado) {
        obtenerCantidadPosicionesBorrador(
            activoSeleccionado
        )
    }

    var proyecto by remember {
        mutableStateOf(borrador.proyecto)
    }

    var kilometraje by remember {
        mutableStateOf(
            borrador.kilometraje
                ?.let(::formatearValorEditable)
                .orEmpty()
        )
    }

    var horometro by remember {
        mutableStateOf(
            borrador.horometro
                ?.let(::formatearValorEditable)
                .orEmpty()
        )
    }

    val huellas = remember {
        mutableStateListOf<String>().apply {
            repeat(12) { indice ->
                add(
                    borrador.huellas
                        .getOrNull(indice)
                        ?.let(::formatearValorEditable)
                        .orEmpty()
                )
            }
        }
    }

    var estadoGeneral by remember {
        mutableStateOf(borrador.estadoGeneral)
    }

    var novedad by remember {
        mutableStateOf(borrador.novedad)
    }

    var nombreTecnico by remember {
        mutableStateOf(borrador.nombreTecnico)
    }

    val codigoActivo = borrador.codigoActivo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Editar borrador de huella",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Activo: $codigoActivo",
            style = MaterialTheme.typography.titleMedium
        )

        activoSeleccionado?.let { activo ->
            Text(
                text = "${activo.marca} ${activo.modelo}".trim(),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = proyecto,
            onValueChange = {
                proyecto = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Proyecto")
            },
            enabled = !guardando
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = kilometraje,
            onValueChange = {
                kilometraje = filtrarNumero(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Kilometraje")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            enabled = !guardando
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = horometro,
            onValueChange = {
                horometro = filtrarNumero(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Horómetro")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            enabled = !guardando
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Profundidad de huella",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "$cantidadPosiciones posiciones habilitadas",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        for (indice in 0 until cantidadPosiciones) {
            OutlinedTextField(
                value = huellas[indice],
                onValueChange = { nuevoValor ->
                    huellas[indice] =
                        filtrarNumero(nuevoValor)
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("P${indice + 1} - milímetros")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                enabled = !guardando
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
            value = estadoGeneral,
            onValueChange = {
                estadoGeneral = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Estado general de las llantas")
            },
            minLines = 2,
            enabled = !guardando
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = novedad,
            onValueChange = {
                novedad = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Novedad presentada")
            },
            minLines = 2,
            enabled = !guardando
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nombreTecnico,
            onValueChange = {
                nombreTecnico = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Nombre del técnico")
            },
            enabled = !guardando
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilterChip(
            selected = true,
            onClick = {},
            label = {
                Text("BORRADOR")
            }
        )

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

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
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                limpiarPosicionesNoAplicables(
                    huellas = huellas,
                    cantidadPosiciones = cantidadPosiciones
                )

                onActualizarBorrador(
                    borrador.id,
                    codigoActivo,
                    proyecto,
                    kilometraje,
                    horometro,
                    huellas.toList(),
                    estadoGeneral,
                    novedad,
                    nombreTecnico
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Guardar cambios")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                limpiarPosicionesNoAplicables(
                    huellas = huellas,
                    cantidadPosiciones = cantidadPosiciones
                )

                onEnviarBorrador(
                    borrador.id,
                    codigoActivo,
                    proyecto,
                    kilometraje,
                    horometro,
                    huellas.toList(),
                    estadoGeneral,
                    novedad,
                    nombreTecnico
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text("Enviar registro")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text("Volver a borradores")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun obtenerCantidadPosicionesBorrador(
    activo: ActivoResumen?
): Int {
    if (activo == null) {
        return 12
    }

    val subtipo =
        activo.subtipo.trim().uppercase()

    val tipo =
        activo.tipo.trim().uppercase()

    val marca =
        activo.marca.trim().uppercase()

    val codigo =
        activo.codigo.trim().uppercase()

    return when {
        subtipo.contains("CAMIONETA") -> 4

        (
                subtipo.contains("VOLQUETA") ||
                        tipo.contains("VOLQUETA") ||
                        codigo.startsWith("VVOLQ")
                ) &&
                marca.contains("SHACMAN") -> 12

        subtipo.contains("VOLQUETA") ||
                tipo.contains("VOLQUETA") ||
                codigo.startsWith("VVOLQ") -> 10

        subtipo.contains("CAMION") ||
                subtipo.contains("CAMIÓN") ||
                subtipo.contains("CABEZAL") ||
                subtipo.contains("TANQUERO") -> 6

        subtipo.contains("RODILLO") ||
                subtipo.contains("COMPACTADOR") -> 2

        subtipo.contains("RETROEXCAVADORA") ||
                subtipo.contains("MINICARGADORA") -> 4

        subtipo.contains("MOTONIVELADORA") -> 6

        else -> 4
    }
}

private fun limpiarPosicionesNoAplicables(
    huellas: MutableList<String>,
    cantidadPosiciones: Int
) {
    for (indice in cantidadPosiciones until 12) {
        huellas[indice] = ""
    }
}

private fun filtrarNumero(
    texto: String
): String {
    return texto.filter { caracter ->
        caracter.isDigit() ||
                caracter == '.' ||
                caracter == ','
    }
}

private fun formatearValorEditable(
    valor: Double
): String {
    return if (valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString()
    }
}