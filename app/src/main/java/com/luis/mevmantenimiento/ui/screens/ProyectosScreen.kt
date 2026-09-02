package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luis.mevmantenimiento.data.ProyectoResumen
import com.luis.mevmantenimiento.data.ProyectosRepository

@Composable
fun ProyectosScreen(
    onVolver: () -> Unit
) {
    var proyectos by remember {
        mutableStateOf<List<ProyectoResumen>>(emptyList())
    }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var proyectoEnEdicion by remember {
        mutableStateOf<ProyectoResumen?>(null)
    }
    var mostrarFormulario by remember { mutableStateOf(false) }

    fun cargarProyectos() {
        cargando = true
        ProyectosRepository.cargar(
            onFinalizado = {
                proyectos = it
                cargando = false
            },
            onError = {
                mensaje = it
                cargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarProyectos()
    }

    if (mostrarFormulario) {
        FormularioProyecto(
            proyecto = proyectoEnEdicion,
            guardando = guardando,
            mensaje = mensaje,
            onGuardar = { proyecto ->
                guardando = true
                mensaje = "Guardando proyecto..."

                ProyectosRepository.guardar(
                    proyecto = proyecto,
                    onFinalizado = {
                        guardando = false
                        mensaje = "Proyecto guardado correctamente."
                        mostrarFormulario = false
                        proyectoEnEdicion = null
                        cargarProyectos()
                    },
                    onError = {
                        guardando = false
                        mensaje = it
                    }
                )
            },
            onVolver = {
                mostrarFormulario = false
                proyectoEnEdicion = null
                mensaje = ""
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Proyectos y ubicaciones",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick = {
                    proyectoEnEdicion = null
                    mensaje = ""
                    mostrarFormulario = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar proyecto")
            }

            if (mensaje.isNotBlank()) {
                Text(mensaje)
            }
        }

        if (cargando) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (proyectos.isEmpty()) {
                    item {
                        Text("Todavía no existen proyectos registrados.")
                    }
                }

                items(proyectos, key = { it.id }) { proyecto ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                proyecto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Ciudad: ${proyecto.ciudad.ifBlank { "Sin registro" }}")
                            Text("Ubicación: ${proyecto.ubicacion.ifBlank { "Sin registro" }}")
                            Text(if (proyecto.activo) "ACTIVO" else "INACTIVO")
                            Button(
                                onClick = {
                                    proyectoEnEdicion = proyecto
                                    mensaje = ""
                                    mostrarFormulario = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Editar")
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Volver a matriz base")
        }
    }
}

@Composable
private fun FormularioProyecto(
    proyecto: ProyectoResumen?,
    guardando: Boolean,
    mensaje: String,
    onGuardar: (ProyectoResumen) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember(proyecto?.id) {
        mutableStateOf(proyecto?.nombre.orEmpty())
    }
    var ciudad by remember(proyecto?.id) {
        mutableStateOf(proyecto?.ciudad.orEmpty())
    }
    var ubicacion by remember(proyecto?.id) {
        mutableStateOf(proyecto?.ubicacion.orEmpty())
    }
    var descripcion by remember(proyecto?.id) {
        mutableStateOf(proyecto?.descripcion.orEmpty())
    }
    var activo by remember(proyecto?.id) {
        mutableStateOf(proyecto?.activo ?: true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (proyecto == null) "Agregar proyecto" else "Editar proyecto",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            nombre,
            { nombre = it },
            Modifier.fillMaxWidth(),
            label = { Text("Nombre *") },
            enabled = !guardando
        )
        OutlinedTextField(
            ciudad,
            { ciudad = it },
            Modifier.fillMaxWidth(),
            label = { Text("Ciudad") },
            enabled = !guardando
        )
        OutlinedTextField(
            ubicacion,
            { ubicacion = it },
            Modifier.fillMaxWidth(),
            label = { Text("Ubicación / campamento") },
            enabled = !guardando
        )
        OutlinedTextField(
            descripcion,
            { descripcion = it },
            Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            enabled = !guardando,
            minLines = 3
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = activo,
                onCheckedChange = { activo = it },
                enabled = !guardando
            )
            Text("Proyecto activo")
        }

        if (mensaje.isNotBlank()) {
            Text(mensaje)
        }

        Button(
            onClick = {
                onGuardar(
                    ProyectoResumen(
                        id = proyecto?.id.orEmpty(),
                        nombre = nombre,
                        ciudad = ciudad,
                        ubicacion = ubicacion,
                        descripcion = descripcion,
                        activo = activo
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando && nombre.isNotBlank()
        ) {
            Text(if (guardando) "Guardando..." else "Guardar proyecto")
        }
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text("Volver")
        }
    }
}
