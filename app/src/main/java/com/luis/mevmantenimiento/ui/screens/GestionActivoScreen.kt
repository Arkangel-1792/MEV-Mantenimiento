package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun GestionActivoScreen(
    activo: ActivoResumen?,
    guardando: Boolean,
    mensaje: String,
    onGuardar: (ActivoResumen) -> Unit,
    onVolver: () -> Unit
) {
    val esNuevo = activo == null

    var codigo by remember(activo?.codigo) {
        mutableStateOf(activo?.codigo.orEmpty())
    }
    var subtipo by remember(activo?.codigo) {
        mutableStateOf(activo?.subtipo.orEmpty())
    }
    var tipo by remember(activo?.codigo) {
        mutableStateOf(activo?.tipo.orEmpty())
    }
    var marca by remember(activo?.codigo) {
        mutableStateOf(activo?.marca.orEmpty())
    }
    var modelo by remember(activo?.codigo) {
        mutableStateOf(activo?.modelo.orEmpty())
    }
    var serie by remember(activo?.codigo) {
        mutableStateOf(activo?.serie.orEmpty())
    }
    var anioFabricacion by remember(activo?.codigo) {
        mutableStateOf(activo?.anioFabricacion?.toString().orEmpty())
    }
    var matricula by remember(activo?.codigo) {
        mutableStateOf(activo?.matricula.orEmpty())
    }
    var indicador by remember(activo?.codigo) {
        mutableStateOf(activo?.indicador?.ifBlank { "AMBOS" } ?: "AMBOS")
    }
    var intervalo by remember(activo?.codigo) {
        mutableStateOf(formatearNumeroActivo(activo?.intervalo))
    }
    var horometro by remember(activo?.codigo) {
        mutableStateOf(formatearNumeroActivo(activo?.horometro))
    }
    var kilometraje by remember(activo?.codigo) {
        mutableStateOf(formatearNumeroActivo(activo?.kilometraje))
    }
    var ubicacion by remember(activo?.codigo) {
        mutableStateOf(activo?.ubicacionActual.orEmpty())
    }
    var status by remember(activo?.codigo) {
        mutableStateOf(activo?.status?.ifBlank { "OPERATIVA" } ?: "OPERATIVA")
    }
    var telemetria by remember(activo?.codigo) {
        mutableStateOf(activo?.telemetria.orEmpty())
    }
    var usaHorometro by remember(activo?.codigo) {
        mutableStateOf(activo?.usaHorometro ?: true)
    }
    var usaKilometraje by remember(activo?.codigo) {
        mutableStateOf(activo?.usaKilometraje ?: true)
    }
    var aplicaVulcanizacion by remember(activo?.codigo) {
        mutableStateOf(activo?.aplicaVulcanizacion ?: true)
    }
    var cantidadPosiciones by remember(activo?.codigo) {
        mutableStateOf(
            (activo?.cantidadPosiciones?.takeIf { it > 0 } ?: 4).toString()
        )
    }
    var configuracionRuedas by remember(activo?.codigo) {
        mutableStateOf(activo?.configuracionRuedas.orEmpty())
    }
    var permitePreventivo by remember(activo?.codigo) {
        mutableStateOf(activo?.permitePreventivo ?: true)
    }
    var permiteCorrectivo by remember(activo?.codigo) {
        mutableStateOf(activo?.permiteCorrectivo ?: true)
    }
    var permiteTomaHuella by remember(activo?.codigo) {
        mutableStateOf(activo?.permiteTomaHuella ?: true)
    }
    var permiteIntervencion by remember(activo?.codigo) {
        mutableStateOf(activo?.permiteIntervencionLlanta ?: true)
    }
    var mensajeLocal by remember(activo?.codigo) {
        mutableStateOf("")
    }

    fun construirActivo(): ActivoResumen? {
        if (codigo.isBlank() || tipo.isBlank() || subtipo.isBlank()) {
            mensajeLocal = "Código, tipo y subtipo son obligatorios."
            return null
        }

        val posiciones = cantidadPosiciones.toIntOrNull() ?: 0
        if (aplicaVulcanizacion && posiciones !in 1..12) {
            mensajeLocal = "Las posiciones deben estar entre 1 y 12."
            return null
        }

        val indicadorNormalizado = indicador.trim().uppercase()
        if (indicadorNormalizado !in setOf("HR", "KM", "AMBOS")) {
            mensajeLocal = "El indicador debe ser HR, KM o AMBOS."
            return null
        }

        mensajeLocal = ""

        return ActivoResumen(
            codigo = codigo.trim().uppercase(),
            subtipo = subtipo.trim(),
            tipo = tipo.trim(),
            marca = marca.trim(),
            modelo = modelo.trim(),
            serie = serie.trim(),
            anioFabricacion = anioFabricacion.toIntOrNull(),
            matricula = matricula.trim(),
            indicador = indicadorNormalizado,
            intervalo = intervalo.numeroDecimal(),
            horometro = horometro.numeroDecimal(),
            kilometraje = kilometraje.numeroDecimal(),
            ubicacionActual = ubicacion.trim(),
            status = status.trim(),
            telemetria = telemetria.trim(),
            usaHorometro = usaHorometro,
            usaKilometraje = usaKilometraje,
            aplicaVulcanizacion = aplicaVulcanizacion,
            cantidadPosiciones = if (aplicaVulcanizacion) posiciones else 0,
            configuracionRuedas = configuracionRuedas.trim(),
            estadoConfiguracion = "CONFIGURADO",
            permitePreventivo = permitePreventivo,
            permiteCorrectivo = permiteCorrectivo,
            permiteTomaHuella = aplicaVulcanizacion && permiteTomaHuella,
            permiteIntervencionLlanta =
                aplicaVulcanizacion && permiteIntervencion
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (esNuevo) "Agregar activo" else "Editar activo",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                CampoActivo(
                    valor = codigo,
                    onValorCambio = { codigo = it.uppercase() },
                    etiqueta = "Código del activo *",
                    habilitado = esNuevo && !guardando
                )
            }
            item { CampoActivo(subtipo, { subtipo = it }, "Subtipo *", !guardando) }
            item { CampoActivo(tipo, { tipo = it }, "Tipo *", !guardando) }
            item { CampoActivo(marca, { marca = it }, "Marca", !guardando) }
            item { CampoActivo(modelo, { modelo = it }, "Modelo", !guardando) }
            item { CampoActivo(serie, { serie = it }, "Serie / chasis", !guardando) }
            item {
                CampoActivo(
                    anioFabricacion,
                    { anioFabricacion = it.filter(Char::isDigit).take(4) },
                    "Año de fabricación",
                    !guardando,
                    KeyboardType.Number
                )
            }
            item { CampoActivo(matricula, { matricula = it }, "Matrícula", !guardando) }
            item {
                CampoActivo(
                    indicador,
                    { indicador = it.uppercase() },
                    "Indicador: HR, KM o AMBOS *",
                    !guardando
                )
            }
            item {
                CampoActivo(
                    intervalo,
                    { intervalo = it.filtrarDecimal() },
                    "Intervalo de mantenimiento",
                    !guardando,
                    KeyboardType.Decimal
                )
            }
            item {
                CampoActivo(
                    horometro,
                    { horometro = it.filtrarDecimal() },
                    "Horómetro actual",
                    !guardando,
                    KeyboardType.Decimal
                )
            }
            item {
                CampoActivo(
                    kilometraje,
                    { kilometraje = it.filtrarDecimal() },
                    "Kilometraje actual",
                    !guardando,
                    KeyboardType.Decimal
                )
            }
            item { CampoActivo(ubicacion, { ubicacion = it }, "Proyecto / ubicación", !guardando) }
            item { CampoActivo(status, { status = it }, "Estado operativo", !guardando) }
            item { CampoActivo(telemetria, { telemetria = it }, "Telemetría", !guardando) }

            item { TituloConfiguracion("Lecturas y formularios") }
            item { OpcionActivo("Usa horómetro", usaHorometro, !guardando) { usaHorometro = it } }
            item { OpcionActivo("Usa kilometraje", usaKilometraje, !guardando) { usaKilometraje = it } }
            item { OpcionActivo("Permite mantenimiento preventivo", permitePreventivo, !guardando) { permitePreventivo = it } }
            item { OpcionActivo("Permite mantenimiento correctivo", permiteCorrectivo, !guardando) { permiteCorrectivo = it } }

            item { TituloConfiguracion("Configuración de llantas") }
            item {
                OpcionActivo(
                    "Aplica vulcanización",
                    aplicaVulcanizacion,
                    !guardando
                ) {
                    aplicaVulcanizacion = it
                }
            }

            if (aplicaVulcanizacion) {
                item {
                    CampoActivo(
                        cantidadPosiciones,
                        {
                            cantidadPosiciones =
                                it.filter(Char::isDigit).take(2)
                        },
                        "Cantidad de posiciones (1 a 12) *",
                        !guardando,
                        KeyboardType.Number
                    )
                }
                item {
                    CampoActivo(
                        configuracionRuedas,
                        { configuracionRuedas = it },
                        "Configuración de ruedas",
                        !guardando
                    )
                }
                item { OpcionActivo("Permite toma de huella", permiteTomaHuella, !guardando) { permiteTomaHuella = it } }
                item { OpcionActivo("Permite intervención de llanta", permiteIntervencion, !guardando) { permiteIntervencion = it } }
            }

            if (mensajeLocal.isNotBlank() || mensaje.isNotBlank()) {
                item {
                    Text(
                        text = mensajeLocal.ifBlank { mensaje },
                        color = if (mensajeLocal.isNotBlank()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        construirActivo()?.let(onGuardar)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar activo")
                }
            }

            item {
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
private fun CampoActivo(
    valor: String,
    onValorCambio: (String) -> Unit,
    etiqueta: String,
    habilitado: Boolean,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambio,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        singleLine = true,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado)
    )
}

@Composable
private fun OpcionActivo(
    etiqueta: String,
    seleccionado: Boolean,
    habilitado: Boolean,
    onSeleccionCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = seleccionado,
            onCheckedChange = onSeleccionCambio,
            enabled = habilitado
        )
        Text(etiqueta)
    }
}

@Composable
private fun TituloConfiguracion(
    texto: String
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

private fun String.filtrarDecimal(): String {
    return filter { caracter ->
        caracter.isDigit() || caracter == '.' || caracter == ','
    }
}

private fun String.numeroDecimal(): Double? {
    return trim().replace(',', '.').toDoubleOrNull()
}

private fun formatearNumeroActivo(
    valor: Double?
): String {
    if (valor == null) {
        return ""
    }

    return if (valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString()
    }
}
