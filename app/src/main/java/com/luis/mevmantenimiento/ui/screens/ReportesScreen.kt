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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.mevmantenimiento.data.FiltrosReporte

@Composable
fun ReportesScreen(
    resumen: ReporteResumen?,
    cargando: Boolean,
    mensaje: String,
    onAplicarFiltros: (FiltrosReporte) -> Unit,
    onLimpiarFiltros: () -> Unit,
    onExportarPdf: () -> Unit,
    onExportarExcel: () -> Unit,
    onVolver: () -> Unit
) {
    var mostrarFiltros by remember {
        mutableStateOf(false)
    }

    var proyecto by remember {
        mutableStateOf("")
    }

    var activo by remember {
        mutableStateOf("")
    }

    var tecnico by remember {
        mutableStateOf("")
    }

    var estado by remember {
        mutableStateOf("")
    }

    var fechaDesde by remember {
        mutableStateOf("")
    }

    var fechaHasta by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            cargando -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Cargando indicadores...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            resumen == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No fue posible cargar los indicadores.",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (mensaje.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onVolver
                    ) {
                        Text("Volver")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 18.dp,
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        EncabezadoReportes()
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                mostrarFiltros = !mostrarFiltros
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (mostrarFiltros) {
                                    "Ocultar filtros"
                                } else {
                                    "Mostrar filtros"
                                }
                            )
                        }
                    }

                    if (mostrarFiltros) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Filtros",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    OutlinedTextField(
                                        value = proyecto,
                                        onValueChange = {
                                            proyecto = it
                                        },
                                        label = {
                                            Text("Proyecto")
                                        },
                                        placeholder = {
                                            Text("Ej.: Villonaco")
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = activo,
                                        onValueChange = {
                                            activo = it
                                        },
                                        label = {
                                            Text("Activo")
                                        },
                                        placeholder = {
                                            Text("Ej.: VVOLQ0100")
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = tecnico,
                                        onValueChange = {
                                            tecnico = it
                                        },
                                        label = {
                                            Text("Técnico / usuario")
                                        },
                                        placeholder = {
                                            Text("Nombre o correo")
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = estado,
                                        onValueChange = {
                                            estado = it.uppercase()
                                        },
                                        label = {
                                            Text("Estado")
                                        },
                                        placeholder = {
                                            Text("ENVIADO, APROBADO o DEVUELTO")
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = fechaDesde,
                                            onValueChange = {
                                                fechaDesde = it
                                            },
                                            label = {
                                                Text("Desde")
                                            },
                                            placeholder = {
                                                Text("dd/MM/yyyy")
                                            },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = fechaHasta,
                                            onValueChange = {
                                                fechaHasta = it
                                            },
                                            label = {
                                                Text("Hasta")
                                            },
                                            placeholder = {
                                                Text("dd/MM/yyyy")
                                            },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onAplicarFiltros(
                                                FiltrosReporte(
                                                    proyecto = proyecto,
                                                    activo = activo,
                                                    tecnico = tecnico,
                                                    estado = estado,
                                                    fechaDesde = fechaDesde,
                                                    fechaHasta = fechaHasta
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Aplicar filtros")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            proyecto = ""
                                            activo = ""
                                            tecnico = ""
                                            estado = ""
                                            fechaDesde = ""
                                            fechaHasta = ""
                                            onLimpiarFiltros()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Limpiar filtros")
                                    }
                                }
                            }
                        }
                    }

                    if (mensaje.isNotBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = mensaje,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        ResumenGeneralCard(resumen)
                    }

                    item {
                        TituloSeccion(
                            titulo = "Mantenimiento",
                            subtitulo = "Estado general de las actividades registradas"
                        )
                    }

                    item {
                        FilaKpis(
                            izquierdaTitulo = "Preventivos",
                            izquierdaValor = resumen.preventivos,
                            derechaTitulo = "Correctivos",
                            derechaValor = resumen.correctivos
                        )
                    }

                    item {
                        FilaKpis(
                            izquierdaTitulo = "Aprobados",
                            izquierdaValor = resumen.mantenimientosAprobados,
                            derechaTitulo = "En revisión",
                            derechaValor = resumen.mantenimientosEnviados,
                            izquierdaColor = Color(0xFF2E7D32)
                        )
                    }

                    item {
                        KpiAncho(
                            titulo = "Devueltos para corrección",
                            valor = resumen.mantenimientosDevueltos,
                            descripcion = if (resumen.mantenimientosDevueltos == 0) {
                                "No existen mantenimientos devueltos."
                            } else {
                                "Registros que requieren corrección antes de ser aprobados."
                            },
                            destacar = resumen.mantenimientosDevueltos > 0
                        )
                    }

                    item {
                        TituloSeccion(
                            titulo = "Vulcanización",
                            subtitulo = "Condición e intervenciones registradas en llantas"
                        )
                    }

                    item {
                        FilaKpis(
                            izquierdaTitulo = "Tomas de huella",
                            izquierdaValor = resumen.totalHuellas,
                            derechaTitulo = "Huellas críticas",
                            derechaValor = resumen.huellasCriticas,
                            derechaColor = if (resumen.huellasCriticas > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    item {
                        KpiAncho(
                            titulo = "Intervenciones de llanta",
                            valor = resumen.totalIntervenciones,
                            descripcion = "Cambios, rotaciones, reparaciones, montajes, desmontajes y bajas."
                        )
                    }

                    item {
                        FilaKpis(
                            izquierdaTitulo = "Cambios",
                            izquierdaValor = resumen.cambios,
                            derechaTitulo = "Rotaciones",
                            derechaValor = resumen.rotaciones
                        )
                    }

                    item {
                        FilaKpis(
                            izquierdaTitulo = "Reparaciones",
                            izquierdaValor = resumen.reparaciones,
                            derechaTitulo = "Montajes",
                            derechaValor = resumen.montajes
                        )
                    }

                    item {
                        FilaKpis(
                            izquierdaTitulo = "Desmontajes",
                            izquierdaValor = resumen.desmontajes,
                            derechaTitulo = "Bajas",
                            derechaValor = resumen.bajas,
                            derechaColor = if (resumen.bajas > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            bottom = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportarExcel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Excel")
                        }

                        Button(
                            onClick = onExportarPdf,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("PDF")
                        }
                    }

                    OutlinedButton(
                        onClick = onVolver,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver al menú")
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoReportes() {
    Column {
        Text(
            text = "Reportes e indicadores",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Resumen operativo consolidado del MEV",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResumenGeneralCard(
    resumen: ReporteResumen
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen general",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatoResumen(
                    titulo = "Mantenimientos",
                    valor = resumen.totalMantenimientos,
                    modifier = Modifier.weight(1f)
                )

                DatoResumen(
                    titulo = "Intervenciones",
                    valor = resumen.totalIntervenciones,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatoResumen(
                    titulo = "Tomas de huella",
                    valor = resumen.totalHuellas,
                    modifier = Modifier.weight(1f)
                )

                DatoResumen(
                    titulo = "Pendientes",
                    valor = resumen.registrosPendientesRevision,
                    modifier = Modifier.weight(1f),
                    colorValor = if (resumen.registrosPendientesRevision > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}

@Composable
private fun DatoResumen(
    titulo: String,
    valor: Int,
    modifier: Modifier = Modifier,
    colorValor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorValor
        )
    }
}

@Composable
private fun TituloSeccion(
    titulo: String,
    subtitulo: String
) {
    Column(
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaKpis(
    izquierdaTitulo: String,
    izquierdaValor: Int,
    derechaTitulo: String,
    derechaValor: Int,
    izquierdaColor: Color = MaterialTheme.colorScheme.onSurface,
    derechaColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCompacto(
            titulo = izquierdaTitulo,
            valor = izquierdaValor,
            colorValor = izquierdaColor,
            modifier = Modifier.weight(1f)
        )

        KpiCompacto(
            titulo = derechaTitulo,
            valor = derechaValor,
            colorValor = derechaColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiCompacto(
    titulo: String,
    valor: Int,
    colorValor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorValor
            )
        }
    }
}

@Composable
private fun KpiAncho(
    titulo: String,
    valor: Int,
    descripcion: String,
    destacar: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (destacar) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (destacar) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}