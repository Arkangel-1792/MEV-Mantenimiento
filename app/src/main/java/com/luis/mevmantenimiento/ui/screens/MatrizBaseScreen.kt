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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ModuloMatrizBase(
    val id: String,
    val titulo: String,
    val descripcion: String
)

@Composable
fun MatrizBaseScreen(
    importandoActivos: Boolean,
    progresoImportacion: String,
    mensajeImportacion: String,
    onSeleccionarModulo: (String) -> Unit,
    onVolver: () -> Unit
)
{
    val modulos = listOf(
        ModuloMatrizBase(
            id = "ACTIVOS",
            titulo = "Activos",
            descripcion = "Consultar, agregar y actualizar los equipos del catálogo maestro."
        ),
        ModuloMatrizBase(
            id = "PROYECTOS",
            titulo = "Proyectos y ubicaciones",
            descripcion = "Administrar los proyectos, ciudades y ubicaciones de los activos."
        ),
        ModuloMatrizBase(
            id = "CONFIGURACION_POSICIONES",
            titulo = "Configuración de posiciones",
            descripcion = "Definir la cantidad de llantas y posiciones P1 a P12 de cada activo."
        ),
        ModuloMatrizBase(
            id = "INDICADORES_INTERVALOS",
            titulo = "Indicadores e intervalos",
            descripcion = "Configurar kilometraje, horómetro e intervalos de mantenimiento."
        ),
        ModuloMatrizBase(
            id = "FORMULARIOS_PERMITIDOS",
            titulo = "Formularios permitidos",
            descripcion = "Definir qué registros puede utilizar cada tipo de activo."
        ),
        ModuloMatrizBase(
            id = "IMPORTAR_EXCEL",
            titulo = "Actualizar desde Excel",
            descripcion = "Cargar o actualizar el catálogo maestro utilizando el archivo INVENTARIO."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Matriz base",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Administración del catálogo maestro y configuración de activos.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (progresoImportacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = progresoImportacion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (mensajeImportacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensajeImportacion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (importandoActivos) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No cierres la aplicación durante la carga.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modulos) { modulo ->
                ModuloMatrizCard(
                    modulo = modulo,
                    onClick = {
                        onSeleccionarModulo(modulo.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al menú principal")
                }
            }
        }
    }
}

@Composable
private fun ModuloMatrizCard(
    modulo: ModuloMatrizBase,
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
                text = modulo.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = modulo.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }
        }
    }
}