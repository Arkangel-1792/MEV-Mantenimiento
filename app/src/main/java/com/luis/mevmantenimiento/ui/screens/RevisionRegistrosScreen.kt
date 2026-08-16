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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    registrosMantenimiento: List<RegistroRevision>,
    registrosHuella: List<RegistroRevisionHuella>,
    registrosIntervencion: List<RegistroRevisionIntervencion>,
    cargando: Boolean,
    mensaje: String,
    onAprobarMantenimiento: (RegistroRevision) -> Unit,
    onDevolverMantenimiento: (RegistroRevision, String) -> Unit,
    onAprobarHuella: (RegistroRevisionHuella) -> Unit,
    onDevolverHuella: (RegistroRevisionHuella, String) -> Unit,
    onAprobarIntervencion: (RegistroRevisionIntervencion) -> Unit,
    onDevolverIntervencion: (RegistroRevisionIntervencion, String) -> Unit,
    onVolver: () -> Unit
) {
    val total = registrosMantenimiento.size + registrosHuella.size + registrosIntervencion.size

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Revisión de registros", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Aprueba o devuelve mantenimientos, tomas de huella e intervenciones.")
            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(mensaje)
            }
            if (cargando) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Procesando información...")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Pendientes: $total")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!cargando && total == 0) {
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No existen registros pendientes de revisión.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (registrosMantenimiento.isNotEmpty()) {
                    item { Text("Mantenimiento", style = MaterialTheme.typography.titleLarge) }
                    items(registrosMantenimiento, key = { "M_${it.id}" }) { registro ->
                        TarjetaRevisionMantenimiento(
                            registro = registro,
                            habilitado = !cargando,
                            onAprobar = { onAprobarMantenimiento(registro) },
                            onDevolver = { motivo ->
                                onDevolverMantenimiento(registro, motivo)
                            }
                        )
                    }
                }

                if (registrosHuella.isNotEmpty()) {
                    item { Text("Toma de huella", style = MaterialTheme.typography.titleLarge) }
                    items(registrosHuella, key = { "H_${it.id}" }) { registro ->
                        TarjetaRevisionHuellaUnificada(
                            registro = registro,
                            habilitado = !cargando,
                            onAprobar = { onAprobarHuella(registro) },
                            onDevolver = { motivo ->
                                onDevolverHuella(registro, motivo)
                            }
                        )
                    }
                }

                if (registrosIntervencion.isNotEmpty()) {
                    item { Text("Intervenciones de llanta", style = MaterialTheme.typography.titleLarge) }
                    items(registrosIntervencion, key = { "I_${it.id}" }) { registro ->
                        TarjetaRevisionIntervencionUnificada(
                            registro = registro,
                            habilitado = !cargando,
                            onAprobar = { onAprobarIntervencion(registro) },
                            onDevolver = { motivo ->
                                onDevolverIntervencion(registro, motivo)
                            }
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Text("Volver al menú principal")
        }
    }
}

@Composable
private fun TarjetaRevisionMantenimiento(
    registro: RegistroRevision,
    habilitado: Boolean,
    onAprobar: () -> Unit,
    onDevolver: (String) -> Unit
) {
    var motivo by remember(registro.id) { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(registro.codigoActivo, style = MaterialTheme.typography.titleMedium)
            Text("Servicio: ${registro.tipoServicio}")
            Text("Kilometraje: ${registro.kilometraje ?: "Sin registro"} · Horómetro: ${registro.horometro ?: "Sin registro"}")
            Text("Acción: ${registro.accionEjecutada.ifBlank { "Sin registrar" }}")
            if (registro.observaciones.isNotBlank()) Text("Observaciones: ${registro.observaciones}")

            AccionesRevision(
                motivo = motivo,
                onMotivoChange = { motivo = it },
                habilitado = habilitado,
                onAprobar = onAprobar,
                onDevolver = { onDevolver(motivo.trim()) },
                textoAprobar = "Aprobar mantenimiento"
            )
        }
    }
}

@Composable
private fun TarjetaRevisionHuellaUnificada(
    registro: RegistroRevisionHuella,
    habilitado: Boolean,
    onAprobar: () -> Unit,
    onDevolver: (String) -> Unit
) {
    var motivo by remember(registro.id) { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(registro.codigoActivo, style = MaterialTheme.typography.titleMedium)
            if (registro.proyecto.isNotBlank()) Text("Proyecto: ${registro.proyecto}")
            Text("Kilometraje: ${registro.kilometraje ?: "Sin registro"} · Horómetro: ${registro.horometro ?: "Sin registro"}")

            val mediciones = registro.huellas.mapIndexedNotNull { indice, huella ->
                huella?.let { "P${indice + 1}: $it mm" }
            }
            if (mediciones.isNotEmpty()) Text("Huellas: ${mediciones.joinToString(" · ")}")
            if (registro.estadoGeneral.isNotBlank()) Text("Estado general: ${registro.estadoGeneral}")
            if (registro.novedad.isNotBlank()) Text("Novedad: ${registro.novedad}")
            if (registro.nombreTecnico.isNotBlank()) Text("Técnico: ${registro.nombreTecnico}")

            AccionesRevision(
                motivo = motivo,
                onMotivoChange = { motivo = it },
                habilitado = habilitado,
                onAprobar = onAprobar,
                onDevolver = { onDevolver(motivo.trim()) },
                textoAprobar = "Aprobar toma de huella"
            )
        }
    }
}

@Composable
private fun TarjetaRevisionIntervencionUnificada(
    registro: RegistroRevisionIntervencion,
    habilitado: Boolean,
    onAprobar: () -> Unit,
    onDevolver: (String) -> Unit
) {
    var motivo by remember(registro.id) { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(registro.codigoActivo, style = MaterialTheme.typography.titleMedium)
            Text("Intervención: ${registro.tipoIntervencion}")
            Text("Posición: ${registro.posicion}")
            Text("Huella: ${registro.huella?.let { "$it mm" } ?: "Sin registrar"}")
            Text("Marca: ${registro.marcaLlanta.ifBlank { "Sin registrar" }}")
            Text("Medida: ${registro.medidaLlanta.ifBlank { "Sin registrar" }}")
            Text("Serie: ${registro.serieLlanta.ifBlank { "Sin registrar" }}")
            if (registro.motivo.isNotBlank()) Text("Motivo: ${registro.motivo}")
            if (registro.observaciones.isNotBlank()) Text("Observaciones: ${registro.observaciones}")
            if (registro.nombreTecnico.isNotBlank()) Text("Técnico: ${registro.nombreTecnico}")

            AccionesRevision(
                motivo = motivo,
                onMotivoChange = { motivo = it },
                habilitado = habilitado,
                onAprobar = onAprobar,
                onDevolver = { onDevolver(motivo.trim()) },
                textoAprobar = "Aprobar intervención"
            )
        }
    }
}

@Composable
private fun AccionesRevision(
    motivo: String,
    onMotivoChange: (String) -> Unit,
    habilitado: Boolean,
    onAprobar: () -> Unit,
    onDevolver: () -> Unit,
    textoAprobar: String
) {
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = motivo,
        onValueChange = onMotivoChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Motivo de devolución") },
        minLines = 2,
        enabled = habilitado
    )

    Button(
        onClick = onAprobar,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(textoAprobar)
    }

    OutlinedButton(
        onClick = onDevolver,
        enabled = habilitado && motivo.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Devolver para corrección")
    }
}