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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp

data class ActivoResumen(
    val codigo: String,
    val subtipo: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val indicador: String,
    val horometro: Double?,
    val kilometraje: Double?,
    val ubicacionActual: String,
    val status: String
)

@Composable
fun ActivosScreen(
    activos: List<ActivoResumen>,
    onSeleccionarActivo: (ActivoResumen) -> Unit,
    onAgregarActivo: () -> Unit,
    onVolver: () -> Unit
) {
    var textoBusqueda by remember {
        mutableStateOf("")
    }

    val activosFiltrados = remember(
        textoBusqueda,
        activos
    ) {
        if (textoBusqueda.isBlank()) {
            activos
        } else {
            val busqueda = textoBusqueda.trim().lowercase()

            activos.filter { activo ->
                activo.codigo.lowercase().contains(busqueda) ||
                        activo.subtipo.lowercase().contains(busqueda) ||
                        activo.tipo.lowercase().contains(busqueda) ||
                        activo.marca.lowercase().contains(busqueda) ||
                        activo.modelo.lowercase().contains(busqueda) ||
                        activo.ubicacionActual.lowercase().contains(busqueda)
            }
        }
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
                text = "Catálogo de activos",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta y administración de los equipos registrados.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Buscar por código, marca, modelo o ubicación")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Registrados: ${activos.size}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Mostrados: ${activosFiltrados.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAgregarActivo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar activo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activosFiltrados.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (activos.isEmpty()) {
                        "Todavía no existen activos cargados en la base de datos."
                    } else {
                        "No se encontraron activos con ese criterio."
                    },
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
                items(activosFiltrados) { activo ->
                    ActivoCard(
                        activo = activo,
                        onClick = {
                            onSeleccionarActivo(activo)
                        }
                    )
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
            Text("Volver a matriz base")
        }
    }
}

@Composable
private fun ActivoCard(
    activo: ActivoResumen,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = activo.codigo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${activo.subtipo} · ${activo.marca} ${activo.modelo}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ubicación: ${activo.ubicacionActual}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = obtenerLecturaPrincipal(activo),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estado: ${activo.status}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver detalle")
            }
        }
    }
}

private fun obtenerLecturaPrincipal(
    activo: ActivoResumen
): String {
    return when (activo.indicador.uppercase()) {
        "HR" -> {
            val lectura = activo.horometro?.toString() ?: "Sin registro"
            "Horómetro: $lectura h"
        }

        "KM" -> {
            val lectura = activo.kilometraje?.toString() ?: "Sin registro"
            "Kilometraje: $lectura km"
        }

        else -> {
            val horometro = activo.horometro?.toString() ?: "Sin registro"
            val kilometraje = activo.kilometraje?.toString() ?: "Sin registro"

            "Horómetro: $horometro h · Kilometraje: $kilometraje km"
        }
    }
}