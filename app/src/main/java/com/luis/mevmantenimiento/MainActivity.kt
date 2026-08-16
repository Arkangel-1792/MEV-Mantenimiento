package com.luis.mevmantenimiento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luis.mevmantenimiento.ui.theme.MEVMantenimientoTheme
import com.luis.mevmantenimiento.ui.screens.MenuPrincipalScreen
import com.luis.mevmantenimiento.ui.screens.MatrizBaseScreen
import com.luis.mevmantenimiento.ui.screens.ActivoResumen
import com.luis.mevmantenimiento.ui.screens.ActivosScreen
import com.luis.mevmantenimiento.data.ImportadorActivos
import com.luis.mevmantenimiento.ui.screens.DetalleActivoScreen
import com.luis.mevmantenimiento.ui.screens.NuevoMantenimientoScreen
import com.luis.mevmantenimiento.data.MantenimientoRepository
import com.luis.mevmantenimiento.ui.screens.BorradorMantenimiento
import com.luis.mevmantenimiento.ui.screens.MisBorradoresScreen
import com.luis.mevmantenimiento.ui.screens.EditarBorradorScreen
import com.luis.mevmantenimiento.ui.screens.MiHistorialScreen
import com.luis.mevmantenimiento.ui.screens.RegistroHistorial
import com.luis.mevmantenimiento.ui.screens.RegistroRevision
import com.luis.mevmantenimiento.ui.screens.RevisionRegistrosScreen
import com.luis.mevmantenimiento.ui.screens.TomaHuellaScreen
import com.luis.mevmantenimiento.data.VulcanizacionRepository
import com.luis.mevmantenimiento.ui.screens.BorradorHuella
import com.luis.mevmantenimiento.ui.screens.MisBorradoresHuellaScreen
import com.luis.mevmantenimiento.ui.screens.EditarBorradorHuellaScreen
import com.luis.mevmantenimiento.ui.screens.HistorialHuellaScreen
import com.luis.mevmantenimiento.ui.screens.RegistroHistorialHuella
import com.luis.mevmantenimiento.ui.screens.RevisionHuellaScreen
import com.luis.mevmantenimiento.ui.screens.RegistroRevisionHuella
import com.luis.mevmantenimiento.ui.screens.IntervencionLlantaScreen
import com.luis.mevmantenimiento.ui.screens.BorradorIntervencionLlanta
import com.luis.mevmantenimiento.ui.screens.MisBorradoresIntervencionScreen
import com.luis.mevmantenimiento.ui.screens.EditarBorradorIntervencionScreen
import com.luis.mevmantenimiento.ui.screens.RegistroHistorialIntervencion
import com.luis.mevmantenimiento.ui.screens.HistorialIntervencionScreen
import com.luis.mevmantenimiento.ui.screens.RegistroRevisionIntervencion
import com.luis.mevmantenimiento.ui.screens.RevisionIntervencionScreen
import com.luis.mevmantenimiento.ui.screens.ReportesScreen
import com.luis.mevmantenimiento.ui.screens.ReporteResumen
import com.luis.mevmantenimiento.data.ReportesRepository
import com.luis.mevmantenimiento.data.ExportadorReportes
import com.luis.mevmantenimiento.data.ReporteDatos
import com.luis.mevmantenimiento.data.FiltrosReporte

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MEVMantenimientoTheme {

                var perfilUsuario by remember {
                    mutableStateOf<PerfilUsuario?>(null)
                }

                var pantallaActual by remember {
                    mutableStateOf("MENU")
                }

                var activos by remember {
                    mutableStateOf<List<ActivoResumen>>(emptyList())
                }
                var activoSeleccionado by remember {
                    mutableStateOf<ActivoResumen?>(null)
                }
                var importandoActivos by remember {
                    mutableStateOf(false)
                }

                var progresoImportacion by remember {
                    mutableStateOf("")
                }

                var mensajeImportacion by remember {
                    mutableStateOf("")
                }
                var mensajeMantenimiento by remember {
                    mutableStateOf("")
                }

                var guardandoMantenimiento by remember {
                    mutableStateOf(false)
                }

                var borradores by remember {
                    mutableStateOf<List<BorradorMantenimiento>>(emptyList())
                }

                var borradorSeleccionado by remember {
                    mutableStateOf<BorradorMantenimiento?>(null)
                }

                var cargandoBorradores by remember {
                    mutableStateOf(false)
                }

                var mensajeBorradores by remember {
                    mutableStateOf("")
                }

                var historial by remember {
                    mutableStateOf<List<RegistroHistorial>>(emptyList())
                }

                var cargandoHistorial by remember {
                    mutableStateOf(false)
                }

                var mensajeHistorial by remember {
                    mutableStateOf("")
                }

                var registrosRevision by remember {
                    mutableStateOf<List<RegistroRevision>>(emptyList())
                }

                var cargandoRevision by remember {
                    mutableStateOf(false)
                }

                var mensajeRevision by remember {
                    mutableStateOf("")
                }

                var guardandoHuella by remember {
                    mutableStateOf(false)
                }

                var mensajeHuella by remember {
                    mutableStateOf("")
                }

                var borradoresHuella by remember {
                    mutableStateOf<List<BorradorHuella>>(emptyList())
                }

                var borradorHuellaSeleccionado by remember {
                    mutableStateOf<BorradorHuella?>(null)
                }

                var cargandoBorradoresHuella by remember {
                    mutableStateOf(false)
                }

                var mensajeBorradoresHuella by remember {
                    mutableStateOf("")
                }

                var historialHuella by remember {
                    mutableStateOf<List<RegistroHistorialHuella>>(emptyList())
                }

                var cargandoHistorialHuella by remember {
                    mutableStateOf(false)
                }

                var mensajeHistorialHuella by remember {
                    mutableStateOf("")
                }

                var registrosRevisionHuella by remember {
                    mutableStateOf<List<RegistroRevisionHuella>>(emptyList())
                }

                var cargandoRevisionHuella by remember {
                    mutableStateOf(false)
                }

                var mensajeRevisionHuella by remember {
                    mutableStateOf("")
                }

                var guardandoIntervencionLlanta by remember {
                    mutableStateOf(false)
                }

                var mensajeIntervencionLlanta by remember {
                    mutableStateOf("")
                }

                var borradoresIntervencion by remember {
                    mutableStateOf<List<BorradorIntervencionLlanta>>(emptyList())
                }

                var borradorIntervencionSeleccionado by remember {
                    mutableStateOf<BorradorIntervencionLlanta?>(null)
                }

                var cargandoBorradoresIntervencion by remember {
                    mutableStateOf(false)
                }

                var mensajeBorradoresIntervencion by remember {
                    mutableStateOf("")
                }

                var historialIntervencion by remember {
                    mutableStateOf<List<RegistroHistorialIntervencion>>(emptyList())
                }

                var cargandoHistorialIntervencion by remember {
                    mutableStateOf(false)
                }

                var mensajeHistorialIntervencion by remember {
                    mutableStateOf("")
                }

                var registrosRevisionIntervencion by remember {
                    mutableStateOf<List<RegistroRevisionIntervencion>>(emptyList())
                }

                var cargandoRevisionIntervencion by remember {
                    mutableStateOf(false)
                }

                var mensajeRevisionIntervencion by remember {
                    mutableStateOf("")
                }

                var resumenReportes by remember {
                    mutableStateOf<ReporteResumen?>(null)
                }

                var reporteCompleto by remember {
                    mutableStateOf<ReporteDatos?>(null)
                }

                var reporteBase by remember {
                    mutableStateOf<ReporteDatos?>(null)
                }

                var cargandoReportes by remember {
                    mutableStateOf(false)
                }

                var mensajeReportes by remember {
                    mutableStateOf("")
                }

                val exportarPdfLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(
                            "application/pdf"
                        )
                    ) { uri ->
                        val reporte = reporteCompleto

                        if (uri != null && reporte != null) {
                            try {
                                contentResolver.openOutputStream(uri)?.use { salida ->
                                    ExportadorReportes.exportarPdf(
                                        outputStream = salida,
                                        reporte = reporte
                                    )
                                }

                                mensajeReportes =
                                    "PDF exportado correctamente."
                            } catch (error: Exception) {
                                mensajeReportes =
                                    "No se pudo exportar el PDF: ${error.message}"
                            }
                        }
                    }

                val exportarExcelLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                    ) { uri ->
                        val reporte = reporteCompleto

                        if (uri != null && reporte != null) {
                            try {
                                contentResolver.openOutputStream(uri)?.use { salida ->
                                    ExportadorReportes.exportarExcel(
                                        outputStream = salida,
                                        reporte = reporte
                                    )
                                }

                                mensajeReportes =
                                    "Excel exportado correctamente."
                            } catch (error: Exception) {
                                mensajeReportes =
                                    "No se pudo exportar el Excel: ${error.message}"
                            }
                        }
                    }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (perfilUsuario == null) {
                            LoginScreen(
                                onLoginSuccess = { perfil ->
                                    perfilUsuario = perfil
                                    pantallaActual = "MENU"
                                }
                            )
                        } else {
                            when (pantallaActual) {
                                "EDITAR_BORRADOR" -> {
                                    borradorSeleccionado?.let { borrador ->
                                        EditarBorradorScreen(
                                            borrador = borrador,
                                            guardando = guardandoMantenimiento,
                                            mensaje = mensajeMantenimiento,

                                            onActualizarBorrador = {
                                                    id,
                                                    tipoServicio,
                                                    kilometraje,
                                                    horometro,
                                                    accionEjecutada,
                                                    observaciones,
                                                    ordenTrabajo,
                                                    numeroPedido ->

                                                guardandoMantenimiento = true
                                                mensajeMantenimiento = "Guardando cambios..."

                                                MantenimientoRepository.actualizarBorrador(
                                                    idRegistro = id,
                                                    tipoServicio = tipoServicio,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    accionEjecutada = accionEjecutada,
                                                    observaciones = observaciones,
                                                    ordenTrabajo = ordenTrabajo,
                                                    numeroPedido = numeroPedido,

                                                    onFinalizado = {
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento =
                                                            "Borrador actualizado correctamente."

                                                        cargandoBorradores = true

                                                        MantenimientoRepository.cargarMisBorradores(
                                                            onFinalizado = { datos ->
                                                                borradores = datos.map { borradorActualizado ->
                                                                    BorradorMantenimiento(
                                                                        id = borradorActualizado["id"]?.toString().orEmpty(),
                                                                        codigoActivo = borradorActualizado["codigoActivo"]?.toString().orEmpty(),
                                                                        tipoServicio = borradorActualizado["tipoServicio"]?.toString().orEmpty(),
                                                                        kilometraje = (borradorActualizado["kilometraje"] as? Number)?.toDouble(),
                                                                        horometro = (borradorActualizado["horometro"] as? Number)?.toDouble(),
                                                                        accionEjecutada = borradorActualizado["accionEjecutada"]?.toString().orEmpty(),
                                                                        observaciones = borradorActualizado["observaciones"]?.toString().orEmpty(),
                                                                        ordenTrabajo = borradorActualizado["ordenTrabajo"]?.toString().orEmpty(),
                                                                        numeroPedido = borradorActualizado["numeroPedido"]?.toString().orEmpty(),
                                                                        estadoRegistro = borradorActualizado["estadoRegistro"]?.toString().orEmpty(),
                                                                        motivoDevolucion = borradorActualizado["motivoDevolucion"]?.toString().orEmpty()
                                                                    )
                                                                }

                                                                borradorSeleccionado = borradores.firstOrNull {
                                                                    it.id == borradorSeleccionado?.id
                                                                }

                                                                cargandoBorradores = false
                                                            },

                                                            onError = { mensaje ->
                                                                cargandoBorradores = false
                                                                mensajeMantenimiento = mensaje
                                                            }
                                                        )
                                                    },

                                                    onError = { mensaje ->
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento = mensaje
                                                    }
                                                )
                                            },

                                            onEnviarBorrador = {
                                                    id,
                                                    tipoServicio,
                                                    kilometraje,
                                                    horometro,
                                                    accionEjecutada,
                                                    observaciones,
                                                    ordenTrabajo,
                                                    numeroPedido ->

                                                guardandoMantenimiento = true
                                                mensajeMantenimiento = "Enviando borrador..."

                                                MantenimientoRepository.enviarBorrador(
                                                    idRegistro = id,
                                                    tipoServicio = tipoServicio,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    accionEjecutada = accionEjecutada,
                                                    observaciones = observaciones,
                                                    ordenTrabajo = ordenTrabajo,
                                                    numeroPedido = numeroPedido,

                                                    onFinalizado = {
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento =
                                                            "Borrador enviado correctamente."
                                                        pantallaActual = "MENU"
                                                    },

                                                    onError = { mensaje ->
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento = mensaje
                                                    }
                                                )
                                            },

                                            onVolver = {
                                                mensajeMantenimiento = ""
                                                pantallaActual = "MIS_BORRADORES"
                                            }
                                        )
                                    }
                                }
                                "REVISION_REGISTROS" -> {
                                    RevisionRegistrosScreen(
                                        registrosMantenimiento = registrosRevision,
                                        registrosHuella = registrosRevisionHuella,
                                        registrosIntervencion = registrosRevisionIntervencion,
                                        cargando = cargandoRevision,
                                        mensaje = mensajeRevision,

                                        onAprobarMantenimiento = { registro ->
                                            cargandoRevision = true
                                            mensajeRevision = "Aprobando mantenimiento..."

                                            MantenimientoRepository.actualizarEstadoRevision(
                                                idRegistro = registro.id,
                                                nuevoEstado = "APROBADO",
                                                onFinalizado = {
                                                    registrosRevision = registrosRevision.filter { it.id != registro.id }
                                                    cargandoRevision = false
                                                    mensajeRevision = "Mantenimiento ${registro.codigoActivo} aprobado correctamente."
                                                },
                                                onError = { mensaje ->
                                                    cargandoRevision = false
                                                    mensajeRevision = mensaje
                                                }
                                            )
                                        },

                                        onDevolverMantenimiento = { registro, motivo ->
                                            cargandoRevision = true
                                            mensajeRevision = "Devolviendo mantenimiento..."

                                            MantenimientoRepository.devolverRegistro(
                                                idRegistro = registro.id,
                                                motivoDevolucion = motivo,
                                                onFinalizado = {
                                                    registrosRevision = registrosRevision.filter { it.id != registro.id }
                                                    cargandoRevision = false
                                                    mensajeRevision = "Mantenimiento ${registro.codigoActivo} devuelto."
                                                },
                                                onError = { mensaje ->
                                                    cargandoRevision = false
                                                    mensajeRevision = mensaje
                                                }
                                            )
                                        },

                                        onAprobarHuella = { registro ->
                                            cargandoRevision = true
                                            mensajeRevision = "Aprobando toma de huella..."

                                            VulcanizacionRepository.aprobarTomaHuella(
                                                idRegistro = registro.id,
                                                onFinalizado = {
                                                    registrosRevisionHuella = registrosRevisionHuella.filter { it.id != registro.id }
                                                    cargandoRevision = false
                                                    mensajeRevision = "Toma de ${registro.codigoActivo} aprobada correctamente."
                                                },
                                                onError = { mensaje ->
                                                    cargandoRevision = false
                                                    mensajeRevision = mensaje
                                                }
                                            )
                                        },

                                        onDevolverHuella = { registro, motivo ->
                                            cargandoRevision = true
                                            mensajeRevision = "Devolviendo toma de huella..."

                                            VulcanizacionRepository.devolverTomaHuella(
                                                idRegistro = registro.id,
                                                motivoDevolucion = motivo,
                                                onFinalizado = {
                                                    registrosRevisionHuella = registrosRevisionHuella.filter { it.id != registro.id }
                                                    cargandoRevision = false
                                                    mensajeRevision = "Toma de ${registro.codigoActivo} devuelta."
                                                },
                                                onError = { mensaje ->
                                                    cargandoRevision = false
                                                    mensajeRevision = mensaje
                                                }
                                            )
                                        },

                                        onAprobarIntervencion = { registro ->
                                            cargandoRevision = true
                                            mensajeRevision = "Aprobando intervención..."

                                            VulcanizacionRepository.aprobarIntervencionLlanta(
                                                idRegistro = registro.id,
                                                onFinalizado = {
                                                    registrosRevisionIntervencion = registrosRevisionIntervencion.filter { it.id != registro.id }
                                                    cargandoRevision = false
                                                    mensajeRevision = "Intervención ${registro.codigoActivo} aprobada correctamente."
                                                },
                                                onError = { mensaje ->
                                                    cargandoRevision = false
                                                    mensajeRevision = mensaje
                                                }
                                            )
                                        },

                                        onDevolverIntervencion = { registro, motivo ->
                                            cargandoRevision = true
                                            mensajeRevision = "Devolviendo intervención..."

                                            VulcanizacionRepository.devolverIntervencionLlanta(
                                                idRegistro = registro.id,
                                                motivoDevolucion = motivo,
                                                onFinalizado = {
                                                    registrosRevisionIntervencion = registrosRevisionIntervencion.filter { it.id != registro.id }
                                                    cargandoRevision = false
                                                    mensajeRevision = "Intervención ${registro.codigoActivo} devuelta."
                                                },
                                                onError = { mensaje ->
                                                    cargandoRevision = false
                                                    mensajeRevision = mensaje
                                                }
                                            )
                                        },

                                        onVolver = {
                                            mensajeRevision = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "REPORTES" -> {
                                    ReportesScreen(
                                        resumen = resumenReportes,
                                        cargando = cargandoReportes,
                                        mensaje = mensajeReportes,

                                        onAplicarFiltros = { filtros ->
                                            val baseActual = reporteBase

                                            if (baseActual != null) {
                                                val filtrado =
                                                    ReportesRepository.aplicarFiltros(
                                                        reporteOriginal = baseActual,
                                                        filtros = filtros
                                                    )

                                                reporteCompleto = filtrado
                                                resumenReportes = filtrado.resumen
                                                mensajeReportes =
                                                    "Filtros aplicados."
                                            }
                                        },

                                        onLimpiarFiltros = {
                                            val baseActual = reporteBase

                                            if (baseActual != null) {
                                                reporteCompleto = baseActual
                                                resumenReportes = baseActual.resumen
                                                mensajeReportes = ""
                                            }
                                        },

                                        onExportarPdf = {
                                            exportarPdfLauncher.launch(
                                                "Reporte_MEV.pdf"
                                            )
                                        },

                                        onExportarExcel = {
                                            exportarExcelLauncher.launch(
                                                "Reporte_MEV.xlsx"
                                            )
                                        },

                                        onVolver = {
                                            mensajeReportes = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "MI_HISTORIAL" -> {
                                    MiHistorialScreen(
                                        registrosMantenimiento = historial,
                                        registrosHuella = historialHuella,
                                        registrosIntervencion = historialIntervencion,
                                        cargando = cargandoHistorial,
                                        mensaje = mensajeHistorial,
                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "MIS_BORRADORES" -> {
                                    MisBorradoresScreen(
                                        borradoresMantenimiento = borradores,
                                        borradoresHuella = borradoresHuella,
                                        borradoresIntervencion = borradoresIntervencion,
                                        cargando = cargandoBorradores,
                                        mensaje = mensajeBorradores,

                                        onSeleccionarMantenimiento = { borrador ->
                                            borradorSeleccionado = borrador
                                            pantallaActual = "EDITAR_BORRADOR"
                                        },

                                        onSeleccionarHuella = { borrador ->
                                            borradorHuellaSeleccionado = borrador
                                            mensajeHuella = ""
                                            pantallaActual = "EDITAR_BORRADOR_HUELLA"
                                        },

                                        onSeleccionarIntervencion = { borrador ->
                                            borradorIntervencionSeleccionado = borrador
                                            mensajeIntervencionLlanta = ""
                                            pantallaActual = "EDITAR_BORRADOR_INTERVENCION"
                                        },

                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "REVISION_HUELLA" -> {
                                    RevisionHuellaScreen(
                                        registros = registrosRevisionHuella,
                                        cargando = cargandoRevisionHuella,
                                        mensaje = mensajeRevisionHuella,

                                        onAprobar = { registro ->
                                            if (!cargandoRevisionHuella) {
                                                cargandoRevisionHuella = true
                                                mensajeRevisionHuella = "Aprobando toma de huella..."

                                                VulcanizacionRepository.aprobarTomaHuella(
                                                    idRegistro = registro.id,
                                                    onFinalizado = {
                                                        registrosRevisionHuella =
                                                            registrosRevisionHuella.filter {
                                                                it.id != registro.id
                                                            }
                                                        cargandoRevisionHuella = false
                                                        mensajeRevisionHuella =
                                                            "Toma de ${registro.codigoActivo} aprobada correctamente."
                                                    },
                                                    onError = { mensaje ->
                                                        cargandoRevisionHuella = false
                                                        mensajeRevisionHuella = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onDevolver = { registro, motivoDevolucion ->
                                            if (!cargandoRevisionHuella) {
                                                cargandoRevisionHuella = true
                                                mensajeRevisionHuella = "Devolviendo toma de huella..."

                                                VulcanizacionRepository.devolverTomaHuella(
                                                    idRegistro = registro.id,
                                                    motivoDevolucion = motivoDevolucion,
                                                    onFinalizado = {
                                                        registrosRevisionHuella =
                                                            registrosRevisionHuella.filter {
                                                                it.id != registro.id
                                                            }
                                                        cargandoRevisionHuella = false
                                                        mensajeRevisionHuella =
                                                            "Toma de ${registro.codigoActivo} devuelta para corrección."
                                                    },
                                                    onError = { mensaje ->
                                                        cargandoRevisionHuella = false
                                                        mensajeRevisionHuella = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onVolver = {
                                            mensajeRevisionHuella = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "HISTORIAL_HUELLA" -> {
                                    HistorialHuellaScreen(
                                        registros = historialHuella,
                                        cargando = cargandoHistorialHuella,
                                        mensaje = mensajeHistorialHuella,
                                        onVolver = {
                                            mensajeHistorialHuella = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "EDITAR_BORRADOR_HUELLA" -> {
                                    borradorHuellaSeleccionado?.let { borrador ->
                                        EditarBorradorHuellaScreen(
                                            borrador = borrador,
                                            activos = activos,
                                            guardando = guardandoHuella,
                                            mensaje = mensajeHuella,

                                            onActualizarBorrador = {
                                                    idRegistro,
                                                    codigoActivo,
                                                    proyecto,
                                                    kilometraje,
                                                    horometro,
                                                    huellas,
                                                    estadoGeneral,
                                                    novedad,
                                                    nombreTecnico ->

                                                if (!guardandoHuella) {
                                                    guardandoHuella = true
                                                    mensajeHuella = "Guardando cambios..."

                                                    VulcanizacionRepository.actualizarBorrador(
                                                        idRegistro = idRegistro,
                                                        codigoActivo = codigoActivo,
                                                        proyecto = proyecto,
                                                        kilometraje = kilometraje,
                                                        horometro = horometro,
                                                        huellas = huellas,
                                                        estadoGeneral = estadoGeneral,
                                                        novedad = novedad,
                                                        nombreTecnico = nombreTecnico,
                                                        onFinalizado = {
                                                            guardandoHuella = false
                                                            mensajeHuella =
                                                                "Borrador actualizado correctamente."
                                                        },
                                                        onError = { mensaje ->
                                                            guardandoHuella = false
                                                            mensajeHuella = mensaje
                                                        }
                                                    )
                                                }
                                            },

                                            onEnviarBorrador = {
                                                    idRegistro,
                                                    codigoActivo,
                                                    proyecto,
                                                    kilometraje,
                                                    horometro,
                                                    huellas,
                                                    estadoGeneral,
                                                    novedad,
                                                    nombreTecnico ->

                                                if (!guardandoHuella) {
                                                    guardandoHuella = true
                                                    mensajeHuella = "Enviando registro..."

                                                    VulcanizacionRepository.enviarBorrador(
                                                        idRegistro = idRegistro,
                                                        codigoActivo = codigoActivo,
                                                        proyecto = proyecto,
                                                        kilometraje = kilometraje,
                                                        horometro = horometro,
                                                        huellas = huellas,
                                                        estadoGeneral = estadoGeneral,
                                                        novedad = novedad,
                                                        nombreTecnico = nombreTecnico,
                                                        onFinalizado = {
                                                            guardandoHuella = false
                                                            mensajeHuella =
                                                                "Registro enviado correctamente."
                                                            pantallaActual = "MENU"
                                                        },
                                                        onError = { mensaje ->
                                                            guardandoHuella = false
                                                            mensajeHuella = mensaje
                                                        }
                                                    )
                                                }
                                            },

                                            onVolver = {
                                                mensajeHuella = ""
                                                pantallaActual = "MIS_BORRADORES_HUELLA"
                                            }
                                        )
                                    }
                                }

                                "MIS_BORRADORES_HUELLA" -> {
                                    MisBorradoresHuellaScreen(
                                        borradores = borradoresHuella,
                                        cargando = cargandoBorradoresHuella,
                                        mensaje = mensajeBorradoresHuella,
                                        onSeleccionarBorrador = { borrador ->
                                            borradorHuellaSeleccionado = borrador
                                            mensajeHuella = ""
                                            pantallaActual = "EDITAR_BORRADOR_HUELLA"
                                        },
                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "TOMA_HUELLA" -> {
                                    TomaHuellaScreen(
                                        activos = activos,
                                        guardando = guardandoHuella,
                                        mensaje = mensajeHuella,

                                        onGuardar = {
                                                codigoActivo,
                                                proyecto,
                                                kilometraje,
                                                horometro,
                                                huellas,
                                                estadoGeneral,
                                                novedad,
                                                nombreTecnico,
                                                estadoRegistro ->

                                            if (!guardandoHuella) {
                                                guardandoHuella = true
                                                mensajeHuella = if (estadoRegistro == "BORRADOR") {
                                                    "Guardando toma de huella..."
                                                } else {
                                                    "Enviando toma de huella..."
                                                }

                                                VulcanizacionRepository.guardarTomaHuella(
                                                    codigoActivo = codigoActivo,
                                                    proyecto = proyecto,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    huellas = huellas,
                                                    estadoGeneral = estadoGeneral,
                                                    novedad = novedad,
                                                    nombreTecnico = nombreTecnico,
                                                    estadoRegistro = estadoRegistro,
                                                    onFinalizado = { idRegistro ->
                                                        guardandoHuella = false
                                                        mensajeHuella = if (estadoRegistro == "BORRADOR") {
                                                            "Toma de huella guardada correctamente. ID: $idRegistro"
                                                        } else {
                                                            "Toma de huella enviada correctamente. ID: $idRegistro"
                                                        }
                                                    },
                                                    onError = { mensaje ->
                                                        guardandoHuella = false
                                                        mensajeHuella = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onVolver = {
                                            mensajeHuella = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "EDITAR_BORRADOR_INTERVENCION" -> {
                                    borradorIntervencionSeleccionado?.let { borrador ->
                                        EditarBorradorIntervencionScreen(
                                            borrador = borrador,
                                            activos = activos,
                                            guardando = guardandoIntervencionLlanta,
                                            mensaje = mensajeIntervencionLlanta,

                                            onActualizarBorrador = {
                                                    idRegistro,
                                                    codigoActivo,
                                                    proyecto,
                                                    kilometraje,
                                                    horometro,
                                                    tipoIntervencion,
                                                    posicion,
                                                    huella,
                                                    marcaLlanta,
                                                    medidaLlanta,
                                                    serieLlanta,
                                                    motivo,
                                                    observaciones,
                                                    nombreTecnico ->

                                                if (!guardandoIntervencionLlanta) {
                                                    guardandoIntervencionLlanta = true
                                                    mensajeIntervencionLlanta =
                                                        "Guardando cambios..."

                                                    VulcanizacionRepository.actualizarBorradorIntervencion(
                                                        idRegistro = idRegistro,
                                                        codigoActivo = codigoActivo,
                                                        proyecto = proyecto,
                                                        kilometraje = kilometraje,
                                                        horometro = horometro,
                                                        tipoIntervencion = tipoIntervencion,
                                                        posicion = posicion,
                                                        huella = huella,
                                                        marcaLlanta = marcaLlanta,
                                                        medidaLlanta = medidaLlanta,
                                                        serieLlanta = serieLlanta,
                                                        motivo = motivo,
                                                        observaciones = observaciones,
                                                        nombreTecnico = nombreTecnico,
                                                        onFinalizado = {
                                                            guardandoIntervencionLlanta = false
                                                            mensajeIntervencionLlanta =
                                                                "Borrador actualizado correctamente."
                                                        },
                                                        onError = { mensaje ->
                                                            guardandoIntervencionLlanta = false
                                                            mensajeIntervencionLlanta = mensaje
                                                        }
                                                    )
                                                }
                                            },

                                            onEnviarBorrador = {
                                                    idRegistro,
                                                    codigoActivo,
                                                    proyecto,
                                                    kilometraje,
                                                    horometro,
                                                    tipoIntervencion,
                                                    posicion,
                                                    huella,
                                                    marcaLlanta,
                                                    medidaLlanta,
                                                    serieLlanta,
                                                    motivo,
                                                    observaciones,
                                                    nombreTecnico ->

                                                if (!guardandoIntervencionLlanta) {
                                                    guardandoIntervencionLlanta = true
                                                    mensajeIntervencionLlanta =
                                                        "Enviando intervención..."

                                                    VulcanizacionRepository.enviarBorradorIntervencion(
                                                        idRegistro = idRegistro,
                                                        codigoActivo = codigoActivo,
                                                        proyecto = proyecto,
                                                        kilometraje = kilometraje,
                                                        horometro = horometro,
                                                        tipoIntervencion = tipoIntervencion,
                                                        posicion = posicion,
                                                        huella = huella,
                                                        marcaLlanta = marcaLlanta,
                                                        medidaLlanta = medidaLlanta,
                                                        serieLlanta = serieLlanta,
                                                        motivo = motivo,
                                                        observaciones = observaciones,
                                                        nombreTecnico = nombreTecnico,
                                                        onFinalizado = {
                                                            guardandoIntervencionLlanta = false
                                                            mensajeIntervencionLlanta =
                                                                "Intervención enviada correctamente."
                                                            pantallaActual = "MENU"
                                                        },
                                                        onError = { mensaje ->
                                                            guardandoIntervencionLlanta = false
                                                            mensajeIntervencionLlanta = mensaje
                                                        }
                                                    )
                                                }
                                            },

                                            onVolver = {
                                                mensajeIntervencionLlanta = ""
                                                pantallaActual = "MIS_BORRADORES_INTERVENCION"
                                            }
                                        )
                                    }
                                }

                                "MIS_BORRADORES_INTERVENCION" -> {
                                    MisBorradoresIntervencionScreen(
                                        borradores = borradoresIntervencion,
                                        cargando = cargandoBorradoresIntervencion,
                                        mensaje = mensajeBorradoresIntervencion,
                                        onSeleccionarBorrador = { borrador ->
                                            borradorIntervencionSeleccionado = borrador
                                            mensajeIntervencionLlanta = ""
                                            pantallaActual = "EDITAR_BORRADOR_INTERVENCION"
                                        },
                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "HISTORIAL_INTERVENCION" -> {
                                    HistorialIntervencionScreen(
                                        registros = historialIntervencion,
                                        cargando = cargandoHistorialIntervencion,
                                        mensaje = mensajeHistorialIntervencion,
                                        onVolver = {
                                            mensajeHistorialIntervencion = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "REVISION_INTERVENCION" -> {
                                    RevisionIntervencionScreen(
                                        registros = registrosRevisionIntervencion,
                                        cargando = cargandoRevisionIntervencion,
                                        mensaje = mensajeRevisionIntervencion,

                                        onAprobar = { registro ->
                                            if (!cargandoRevisionIntervencion) {
                                                cargandoRevisionIntervencion = true
                                                mensajeRevisionIntervencion =
                                                    "Aprobando intervención..."

                                                VulcanizacionRepository.aprobarIntervencionLlanta(
                                                    idRegistro = registro.id,
                                                    onFinalizado = {
                                                        registrosRevisionIntervencion =
                                                            registrosRevisionIntervencion.filter {
                                                                it.id != registro.id
                                                            }
                                                        cargandoRevisionIntervencion = false
                                                        mensajeRevisionIntervencion =
                                                            "Intervención ${registro.codigoActivo} aprobada."
                                                    },
                                                    onError = { mensaje ->
                                                        cargandoRevisionIntervencion = false
                                                        mensajeRevisionIntervencion = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onDevolver = { registro, motivoDevolucion ->
                                            if (!cargandoRevisionIntervencion) {
                                                cargandoRevisionIntervencion = true
                                                mensajeRevisionIntervencion =
                                                    "Devolviendo intervención..."

                                                VulcanizacionRepository.devolverIntervencionLlanta(
                                                    idRegistro = registro.id,
                                                    motivoDevolucion = motivoDevolucion,
                                                    onFinalizado = {
                                                        registrosRevisionIntervencion =
                                                            registrosRevisionIntervencion.filter {
                                                                it.id != registro.id
                                                            }
                                                        cargandoRevisionIntervencion = false
                                                        mensajeRevisionIntervencion =
                                                            "Intervención ${registro.codigoActivo} devuelta para corrección."
                                                    },
                                                    onError = { mensaje ->
                                                        cargandoRevisionIntervencion = false
                                                        mensajeRevisionIntervencion = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onVolver = {
                                            mensajeRevisionIntervencion = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "INTERVENCION_LLANTA" -> {
                                    IntervencionLlantaScreen(
                                        activos = activos,
                                        guardando = guardandoIntervencionLlanta,
                                        mensaje = mensajeIntervencionLlanta,

                                        onGuardarBorrador = {
                                                codigoActivo,
                                                proyecto,
                                                kilometraje,
                                                horometro,
                                                tipoIntervencion,
                                                posicion,
                                                huella,
                                                marcaLlanta,
                                                medidaLlanta,
                                                serieLlanta,
                                                motivo,
                                                observaciones,
                                                nombreTecnico ->

                                            if (!guardandoIntervencionLlanta) {
                                                guardandoIntervencionLlanta = true
                                                mensajeIntervencionLlanta =
                                                    "Guardando borrador de intervención..."

                                                VulcanizacionRepository.guardarIntervencionLlanta(
                                                    codigoActivo = codigoActivo,
                                                    proyecto = proyecto,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    tipoIntervencion = tipoIntervencion,
                                                    posicion = posicion,
                                                    huella = huella,
                                                    marcaLlanta = marcaLlanta,
                                                    medidaLlanta = medidaLlanta,
                                                    serieLlanta = serieLlanta,
                                                    motivo = motivo,
                                                    observaciones = observaciones,
                                                    nombreTecnico = nombreTecnico,
                                                    estadoRegistro =
                                                        VulcanizacionRepository.ESTADO_BORRADOR,

                                                    onFinalizado = { idRegistro ->
                                                        guardandoIntervencionLlanta = false
                                                        mensajeIntervencionLlanta =
                                                            "Borrador guardado correctamente. ID: $idRegistro"
                                                    },

                                                    onError = { mensaje ->
                                                        guardandoIntervencionLlanta = false
                                                        mensajeIntervencionLlanta = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onEnviar = {
                                                codigoActivo,
                                                proyecto,
                                                kilometraje,
                                                horometro,
                                                tipoIntervencion,
                                                posicion,
                                                huella,
                                                marcaLlanta,
                                                medidaLlanta,
                                                serieLlanta,
                                                motivo,
                                                observaciones,
                                                nombreTecnico ->

                                            if (!guardandoIntervencionLlanta) {
                                                guardandoIntervencionLlanta = true
                                                mensajeIntervencionLlanta =
                                                    "Enviando intervención de llanta..."

                                                VulcanizacionRepository.guardarIntervencionLlanta(
                                                    codigoActivo = codigoActivo,
                                                    proyecto = proyecto,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    tipoIntervencion = tipoIntervencion,
                                                    posicion = posicion,
                                                    huella = huella,
                                                    marcaLlanta = marcaLlanta,
                                                    medidaLlanta = medidaLlanta,
                                                    serieLlanta = serieLlanta,
                                                    motivo = motivo,
                                                    observaciones = observaciones,
                                                    nombreTecnico = nombreTecnico,
                                                    estadoRegistro =
                                                        VulcanizacionRepository.ESTADO_ENVIADO,

                                                    onFinalizado = { idRegistro ->
                                                        guardandoIntervencionLlanta = false
                                                        mensajeIntervencionLlanta =
                                                            "Intervención enviada correctamente. ID: $idRegistro"
                                                    },

                                                    onError = { mensaje ->
                                                        guardandoIntervencionLlanta = false
                                                        mensajeIntervencionLlanta = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onVolver = {
                                            mensajeIntervencionLlanta = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "NUEVO_MANTENIMIENTO" -> {
                                    NuevoMantenimientoScreen(
                                        activos = activos,
                                        guardandoMantenimiento = guardandoMantenimiento,
                                        mensajeMantenimiento = mensajeMantenimiento,

                                        onGuardarBorrador = {
                                                codigoActivo,
                                                tipoServicio,
                                                kilometraje,
                                                horometro,
                                                accionEjecutada,
                                                observaciones,
                                                ordenTrabajo,
                                                numeroPedido ->

                                            if (!guardandoMantenimiento) {
                                                guardandoMantenimiento = true
                                                mensajeMantenimiento = "Guardando borrador..."

                                                MantenimientoRepository.guardarRegistro(
                                                    codigoActivo = codigoActivo,
                                                    tipoServicio = tipoServicio,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    accionEjecutada = accionEjecutada,
                                                    observaciones = observaciones,
                                                    ordenTrabajo = ordenTrabajo,
                                                    numeroPedido = numeroPedido,
                                                    estadoRegistro = "BORRADOR",

                                                    onFinalizado = { idRegistro ->
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento =
                                                            "Borrador guardado correctamente. ID: $idRegistro"
                                                    },

                                                    onError = { mensaje ->
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onEnviar = {
                                                codigoActivo,
                                                tipoServicio,
                                                kilometraje,
                                                horometro,
                                                accionEjecutada,
                                                observaciones,
                                                ordenTrabajo,
                                                numeroPedido ->

                                            if (!guardandoMantenimiento) {
                                                guardandoMantenimiento = true
                                                mensajeMantenimiento = "Enviando registro..."

                                                MantenimientoRepository.guardarRegistro(
                                                    codigoActivo = codigoActivo,
                                                    tipoServicio = tipoServicio,
                                                    kilometraje = kilometraje,
                                                    horometro = horometro,
                                                    accionEjecutada = accionEjecutada,
                                                    observaciones = observaciones,
                                                    ordenTrabajo = ordenTrabajo,
                                                    numeroPedido = numeroPedido,
                                                    estadoRegistro = "ENVIADO",

                                                    onFinalizado = { idRegistro ->
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento =
                                                            "Registro enviado correctamente. ID: $idRegistro"
                                                    },

                                                    onError = { mensaje ->
                                                        guardandoMantenimiento = false
                                                        mensajeMantenimiento = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }
                                "DETALLE_ACTIVO" -> {
                                    activoSeleccionado?.let { activo ->
                                        DetalleActivoScreen(
                                            activo = activo,
                                            onEditar = {
                                                // Después crearemos el formulario de edición.
                                            },
                                            onVolver = {
                                                pantallaActual = "ACTIVOS"
                                            }
                                        )
                                    }
                                }
                                "ACTIVOS" -> {
                                    ActivosScreen(
                                        activos = activos,
                                        onSeleccionarActivo = { activo ->
                                            activoSeleccionado = activo
                                            pantallaActual = "DETALLE_ACTIVO"
                                        },
                                        onAgregarActivo = {
                                            // Después crearemos el formulario para agregar activos.
                                        },
                                        onVolver = {
                                            pantallaActual = "MATRIZ_BASE"
                                        }
                                    )
                                }
                                "MATRIZ_BASE" -> {
                                    MatrizBaseScreen(
                                        importandoActivos = importandoActivos,
                                        progresoImportacion = progresoImportacion,
                                        mensajeImportacion = mensajeImportacion,
                                        onSeleccionarModulo = { modulo ->

                                            when (modulo) {

                                                "ACTIVOS" -> {
                                                    mensajeImportacion = "Cargando activos..."

                                                    ImportadorActivos.cargarActivos(
                                                        onFinalizado = { datos ->

                                                            activos = datos.map { activo ->
                                                                ActivoResumen(
                                                                    codigo = activo["codigo"]?.toString().orEmpty(),
                                                                    subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                    tipo = activo["tipo"]?.toString().orEmpty(),
                                                                    marca = activo["marca"]?.toString().orEmpty(),
                                                                    modelo = activo["modelo"]?.toString().orEmpty(),
                                                                    indicador = activo["indicador"]?.toString().orEmpty(),
                                                                    horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                    kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                    ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                    status = activo["status"]?.toString().orEmpty()
                                                                )
                                                            }.sortedBy { activo ->
                                                                activo.codigo
                                                            }

                                                            mensajeImportacion = ""
                                                            pantallaActual = "ACTIVOS"
                                                        },

                                                        onError = { mensaje ->
                                                            mensajeImportacion = mensaje
                                                        }
                                                    )
                                                }

                                                "IMPORTAR_EXCEL" -> {
                                                    if (!importandoActivos) {

                                                        importandoActivos = true
                                                        progresoImportacion = "Preparando importación..."
                                                        mensajeImportacion = ""

                                                        ImportadorActivos.importar(
                                                            context = this@MainActivity,

                                                            onProgreso = { actual, total ->
                                                                progresoImportacion =
                                                                    "Importando activos: $actual de $total"
                                                            },

                                                            onFinalizado = { cantidad ->
                                                                importandoActivos = false
                                                                progresoImportacion = ""
                                                                mensajeImportacion =
                                                                    "Importación finalizada: $cantidad activos cargados."
                                                            },

                                                            onError = { mensaje ->
                                                                importandoActivos = false
                                                                progresoImportacion = ""
                                                                mensajeImportacion = mensaje
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        },

                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                else -> {
                                    // Menú principal existente
                                    MenuPrincipalScreen(
                                        perfil = perfilUsuario!!,
                                        onSeleccionarOpcion = { opcion ->
                                            when (opcion) {

                                                "Matriz base" -> {
                                                    pantallaActual = "MATRIZ_BASE"
                                                }

                                                "Nuevo mantenimiento" -> {
                                                    if (activos.isEmpty()) {
                                                        ImportadorActivos.cargarActivos(
                                                            onFinalizado = { datos ->
                                                                activos = datos.map { activo ->
                                                                    ActivoResumen(
                                                                        codigo = activo["codigo"]?.toString().orEmpty(),
                                                                        subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                        tipo = activo["tipo"]?.toString().orEmpty(),
                                                                        marca = activo["marca"]?.toString().orEmpty(),
                                                                        modelo = activo["modelo"]?.toString().orEmpty(),
                                                                        indicador = activo["indicador"]?.toString().orEmpty(),
                                                                        horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                        kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                        ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                        status = activo["status"]?.toString().orEmpty()
                                                                    )
                                                                }.sortedBy { activo ->
                                                                    activo.codigo
                                                                }

                                                                pantallaActual = "NUEVO_MANTENIMIENTO"
                                                            },

                                                            onError = { mensaje ->
                                                                mensajeImportacion = mensaje
                                                            }
                                                        )
                                                    } else {
                                                        pantallaActual = "NUEVO_MANTENIMIENTO"
                                                    }
                                                }
                                                "Toma general de huella" -> {
                                                    mensajeHuella = ""

                                                    if (activos.isEmpty()) {
                                                        ImportadorActivos.cargarActivos(
                                                            onFinalizado = { datos ->
                                                                activos = datos.map { activo ->
                                                                    ActivoResumen(
                                                                        codigo = activo["codigo"]?.toString().orEmpty(),
                                                                        subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                        tipo = activo["tipo"]?.toString().orEmpty(),
                                                                        marca = activo["marca"]?.toString().orEmpty(),
                                                                        modelo = activo["modelo"]?.toString().orEmpty(),
                                                                        indicador = activo["indicador"]?.toString().orEmpty(),
                                                                        horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                        kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                        ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                        status = activo["status"]?.toString().orEmpty()
                                                                    )
                                                                }.sortedBy { activo -> activo.codigo }

                                                                pantallaActual = "TOMA_HUELLA"
                                                            },
                                                            onError = { mensaje ->
                                                                mensajeHuella = mensaje
                                                            }
                                                        )
                                                    } else {
                                                        pantallaActual = "TOMA_HUELLA"
                                                    }
                                                }

                                                "Intervención de llanta" -> {
                                                    mensajeIntervencionLlanta = ""

                                                    if (activos.isEmpty()) {
                                                        ImportadorActivos.cargarActivos(
                                                            onFinalizado = { datos ->
                                                                activos = datos.map { activo ->
                                                                    ActivoResumen(
                                                                        codigo = activo["codigo"]?.toString().orEmpty(),
                                                                        subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                        tipo = activo["tipo"]?.toString().orEmpty(),
                                                                        marca = activo["marca"]?.toString().orEmpty(),
                                                                        modelo = activo["modelo"]?.toString().orEmpty(),
                                                                        indicador = activo["indicador"]?.toString().orEmpty(),
                                                                        horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                        kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                        ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                        status = activo["status"]?.toString().orEmpty()
                                                                    )
                                                                }.sortedBy { activo ->
                                                                    activo.codigo
                                                                }

                                                                pantallaActual = "INTERVENCION_LLANTA"
                                                            },

                                                            onError = { mensaje ->
                                                                mensajeIntervencionLlanta = mensaje
                                                            }
                                                        )
                                                    } else {
                                                        pantallaActual = "INTERVENCION_LLANTA"
                                                    }
                                                }

                                                "Borradores de intervención" -> {
                                                    cargandoBorradoresIntervencion = true
                                                    mensajeBorradoresIntervencion = ""
                                                    borradoresIntervencion = emptyList()

                                                    fun cargarBorradoresIntervencion() {
                                                        VulcanizacionRepository.cargarMisBorradoresIntervencion(
                                                            onFinalizado = { datos ->
                                                                borradoresIntervencion = datos.map { registro ->
                                                                    BorradorIntervencionLlanta(
                                                                        id = registro["id"]?.toString().orEmpty(),
                                                                        codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                        proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                        kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                        horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                        tipoIntervencion = registro["tipoIntervencion"]?.toString().orEmpty(),
                                                                        posicion = registro["posicion"]?.toString().orEmpty(),
                                                                        huella = (registro["huella"] as? Number)?.toDouble(),
                                                                        marcaLlanta = registro["marcaLlanta"]?.toString().orEmpty(),
                                                                        medidaLlanta = registro["medidaLlanta"]?.toString().orEmpty(),
                                                                        serieLlanta = registro["serieLlanta"]?.toString().orEmpty(),
                                                                        motivo = registro["motivo"]?.toString().orEmpty(),
                                                                        observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                        nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                        estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                        motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                    )
                                                                }
                                                                cargandoBorradoresIntervencion = false
                                                                pantallaActual = "MIS_BORRADORES_INTERVENCION"
                                                            },
                                                            onError = { mensaje ->
                                                                cargandoBorradoresIntervencion = false
                                                                mensajeBorradoresIntervencion = mensaje
                                                                pantallaActual = "MIS_BORRADORES_INTERVENCION"
                                                            }
                                                        )
                                                    }

                                                    if (activos.isEmpty()) {
                                                        ImportadorActivos.cargarActivos(
                                                            onFinalizado = { datos ->
                                                                activos = datos.map { activo ->
                                                                    ActivoResumen(
                                                                        codigo = activo["codigo"]?.toString().orEmpty(),
                                                                        subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                        tipo = activo["tipo"]?.toString().orEmpty(),
                                                                        marca = activo["marca"]?.toString().orEmpty(),
                                                                        modelo = activo["modelo"]?.toString().orEmpty(),
                                                                        indicador = activo["indicador"]?.toString().orEmpty(),
                                                                        horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                        kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                        ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                        status = activo["status"]?.toString().orEmpty()
                                                                    )
                                                                }.sortedBy { it.codigo }

                                                                cargarBorradoresIntervencion()
                                                            },
                                                            onError = { mensaje ->
                                                                cargandoBorradoresIntervencion = false
                                                                mensajeBorradoresIntervencion = mensaje
                                                                pantallaActual = "MIS_BORRADORES_INTERVENCION"
                                                            }
                                                        )
                                                    } else {
                                                        cargarBorradoresIntervencion()
                                                    }
                                                }

                                                "Historial de intervenciones" -> {
                                                    cargandoHistorialIntervencion = true
                                                    mensajeHistorialIntervencion = ""
                                                    historialIntervencion = emptyList()

                                                    VulcanizacionRepository.cargarMiHistorialIntervencion(
                                                        onFinalizado = { datos ->
                                                            historialIntervencion = datos.map { registro ->
                                                                RegistroHistorialIntervencion(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    tipoIntervencion = registro["tipoIntervencion"]?.toString().orEmpty(),
                                                                    posicion = registro["posicion"]?.toString().orEmpty(),
                                                                    huella = (registro["huella"] as? Number)?.toDouble(),
                                                                    marcaLlanta = registro["marcaLlanta"]?.toString().orEmpty(),
                                                                    medidaLlanta = registro["medidaLlanta"]?.toString().orEmpty(),
                                                                    serieLlanta = registro["serieLlanta"]?.toString().orEmpty(),
                                                                    motivo = registro["motivo"]?.toString().orEmpty(),
                                                                    observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                    motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                )
                                                            }
                                                            cargandoHistorialIntervencion = false
                                                            pantallaActual = "HISTORIAL_INTERVENCION"
                                                        },
                                                        onError = { mensaje ->
                                                            cargandoHistorialIntervencion = false
                                                            mensajeHistorialIntervencion = mensaje
                                                            pantallaActual = "HISTORIAL_INTERVENCION"
                                                        }
                                                    )
                                                }

                                                "Revisión de intervenciones" -> {
                                                    cargandoRevisionIntervencion = true
                                                    mensajeRevisionIntervencion = ""
                                                    registrosRevisionIntervencion = emptyList()

                                                    VulcanizacionRepository.cargarIntervencionesPendientesRevision(
                                                        onFinalizado = { datos ->
                                                            registrosRevisionIntervencion = datos.map { registro ->
                                                                RegistroRevisionIntervencion(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    tipoIntervencion = registro["tipoIntervencion"]?.toString().orEmpty(),
                                                                    posicion = registro["posicion"]?.toString().orEmpty(),
                                                                    huella = (registro["huella"] as? Number)?.toDouble(),
                                                                    marcaLlanta = registro["marcaLlanta"]?.toString().orEmpty(),
                                                                    medidaLlanta = registro["medidaLlanta"]?.toString().orEmpty(),
                                                                    serieLlanta = registro["serieLlanta"]?.toString().orEmpty(),
                                                                    motivo = registro["motivo"]?.toString().orEmpty(),
                                                                    observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    uidUsuario = registro["uidUsuario"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty()
                                                                )
                                                            }
                                                            cargandoRevisionIntervencion = false
                                                            pantallaActual = "REVISION_INTERVENCION"
                                                        },
                                                        onError = { mensaje ->
                                                            cargandoRevisionIntervencion = false
                                                            mensajeRevisionIntervencion = mensaje
                                                            pantallaActual = "REVISION_INTERVENCION"
                                                        }
                                                    )
                                                }

                                                "Revisión de huellas" -> {
                                                    cargandoRevisionHuella = true
                                                    mensajeRevisionHuella = ""
                                                    registrosRevisionHuella = emptyList()

                                                    VulcanizacionRepository.cargarRegistrosPendientesRevision(
                                                        onFinalizado = { datos ->
                                                            registrosRevisionHuella = datos.map { registro ->
                                                                RegistroRevisionHuella(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    huellas = (1..12).map { posicion ->
                                                                        (registro["P$posicion"] as? Number)?.toDouble()
                                                                    },
                                                                    estadoGeneral = registro["estadoGeneral"]?.toString().orEmpty(),
                                                                    novedad = registro["novedad"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    uidUsuario = registro["uidUsuario"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty()
                                                                )
                                                            }

                                                            cargandoRevisionHuella = false
                                                            pantallaActual = "REVISION_HUELLA"
                                                        },
                                                        onError = { mensaje ->
                                                            cargandoRevisionHuella = false
                                                            mensajeRevisionHuella = mensaje
                                                            pantallaActual = "REVISION_HUELLA"
                                                        }
                                                    )
                                                }

                                                "Historial de huellas" -> {
                                                    cargandoHistorialHuella = true
                                                    mensajeHistorialHuella = ""
                                                    historialHuella = emptyList()

                                                    VulcanizacionRepository.cargarMiHistorial(
                                                        onFinalizado = { datos ->
                                                            historialHuella = datos.map { registro ->
                                                                RegistroHistorialHuella(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    huellas = (1..12).map { posicion ->
                                                                        (registro["P$posicion"] as? Number)?.toDouble()
                                                                    },
                                                                    estadoGeneral = registro["estadoGeneral"]?.toString().orEmpty(),
                                                                    novedad = registro["novedad"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                    motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                )
                                                            }

                                                            cargandoHistorialHuella = false
                                                            pantallaActual = "HISTORIAL_HUELLA"
                                                        },
                                                        onError = { mensaje ->
                                                            cargandoHistorialHuella = false
                                                            mensajeHistorialHuella = mensaje
                                                            pantallaActual = "HISTORIAL_HUELLA"
                                                        }
                                                    )
                                                }

                                                "Borradores de huella" -> {
                                                    cargandoBorradoresHuella = true
                                                    mensajeBorradoresHuella = ""
                                                    borradoresHuella = emptyList()

                                                    fun cargarBorradores() {
                                                        VulcanizacionRepository.cargarMisBorradores(
                                                            onFinalizado = { datos ->
                                                                borradoresHuella = datos.map { registro ->
                                                                    BorradorHuella(
                                                                        id = registro["id"]?.toString().orEmpty(),
                                                                        codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                        proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                        kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                        horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                        huellas = (1..12).map { posicion ->
                                                                            (registro["P$posicion"] as? Number)?.toDouble()
                                                                        },
                                                                        estadoGeneral = registro["estadoGeneral"]?.toString().orEmpty(),
                                                                        novedad = registro["novedad"]?.toString().orEmpty(),
                                                                        nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                        estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty()
                                                                    )
                                                                }

                                                                cargandoBorradoresHuella = false
                                                                pantallaActual = "MIS_BORRADORES_HUELLA"
                                                            },
                                                            onError = { mensaje ->
                                                                cargandoBorradoresHuella = false
                                                                mensajeBorradoresHuella = mensaje
                                                                pantallaActual = "MIS_BORRADORES_HUELLA"
                                                            }
                                                        )
                                                    }

                                                    if (activos.isEmpty()) {
                                                        ImportadorActivos.cargarActivos(
                                                            onFinalizado = { datos ->
                                                                activos = datos.map { activo ->
                                                                    ActivoResumen(
                                                                        codigo = activo["codigo"]?.toString().orEmpty(),
                                                                        subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                        tipo = activo["tipo"]?.toString().orEmpty(),
                                                                        marca = activo["marca"]?.toString().orEmpty(),
                                                                        modelo = activo["modelo"]?.toString().orEmpty(),
                                                                        indicador = activo["indicador"]?.toString().orEmpty(),
                                                                        horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                        kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                        ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                        status = activo["status"]?.toString().orEmpty()
                                                                    )
                                                                }.sortedBy { activo ->
                                                                    activo.codigo
                                                                }

                                                                cargarBorradores()
                                                            },
                                                            onError = { mensaje ->
                                                                cargandoBorradoresHuella = false
                                                                mensajeBorradoresHuella = mensaje
                                                                pantallaActual = "MIS_BORRADORES_HUELLA"
                                                            }
                                                        )
                                                    } else {
                                                        cargarBorradores()
                                                    }
                                                }

                                                "Mis borradores" -> {
                                                    cargandoBorradores = true
                                                    mensajeBorradores = ""
                                                    borradores = emptyList()
                                                    borradoresHuella = emptyList()
                                                    borradoresIntervencion = emptyList()

                                                    fun cargarCentroBorradores() {
                                                        var cargasPendientes = 3

                                                        fun finalizarCarga() {
                                                            cargasPendientes--

                                                            if (cargasPendientes <= 0) {
                                                                cargandoBorradores = false
                                                                pantallaActual = "MIS_BORRADORES"
                                                            }
                                                        }

                                                        fun registrarError(mensaje: String) {
                                                            mensajeBorradores =
                                                                if (mensajeBorradores.isBlank()) {
                                                                    mensaje
                                                                } else {
                                                                    mensajeBorradores + "\n" + mensaje
                                                                }

                                                            finalizarCarga()
                                                        }

                                                        MantenimientoRepository.cargarMisBorradores(
                                                            onFinalizado = { datos ->
                                                                borradores = datos.map { registro ->
                                                                    BorradorMantenimiento(
                                                                        id = registro["id"]?.toString().orEmpty(),
                                                                        codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                        tipoServicio = registro["tipoServicio"]?.toString().orEmpty(),
                                                                        kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                        horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                        accionEjecutada = registro["accionEjecutada"]?.toString().orEmpty(),
                                                                        observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                        ordenTrabajo = registro["ordenTrabajo"]?.toString().orEmpty(),
                                                                        numeroPedido = registro["numeroPedido"]?.toString().orEmpty(),
                                                                        estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                        motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                    )
                                                                }

                                                                finalizarCarga()
                                                            },
                                                            onError = { mensaje ->
                                                                registrarError(
                                                                    "Mantenimiento: $mensaje"
                                                                )
                                                            }
                                                        )

                                                        VulcanizacionRepository.cargarMisBorradores(
                                                            onFinalizado = { datos ->
                                                                borradoresHuella = datos.map { registro ->
                                                                    BorradorHuella(
                                                                        id = registro["id"]?.toString().orEmpty(),
                                                                        codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                        proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                        kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                        horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                        huellas = (1..12).map { posicion ->
                                                                            (registro["P$posicion"] as? Number)?.toDouble()
                                                                        },
                                                                        estadoGeneral = registro["estadoGeneral"]?.toString().orEmpty(),
                                                                        novedad = registro["novedad"]?.toString().orEmpty(),
                                                                        nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                        estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                        motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                    )
                                                                }

                                                                finalizarCarga()
                                                            },
                                                            onError = { mensaje ->
                                                                registrarError(
                                                                    "Toma de huella: $mensaje"
                                                                )
                                                            }
                                                        )

                                                        VulcanizacionRepository.cargarMisBorradoresIntervencion(
                                                            onFinalizado = { datos ->
                                                                borradoresIntervencion = datos.map { registro ->
                                                                    BorradorIntervencionLlanta(
                                                                        id = registro["id"]?.toString().orEmpty(),
                                                                        codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                        proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                        kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                        horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                        tipoIntervencion = registro["tipoIntervencion"]?.toString().orEmpty(),
                                                                        posicion = registro["posicion"]?.toString().orEmpty(),
                                                                        huella = (registro["huella"] as? Number)?.toDouble(),
                                                                        marcaLlanta = registro["marcaLlanta"]?.toString().orEmpty(),
                                                                        medidaLlanta = registro["medidaLlanta"]?.toString().orEmpty(),
                                                                        serieLlanta = registro["serieLlanta"]?.toString().orEmpty(),
                                                                        motivo = registro["motivo"]?.toString().orEmpty(),
                                                                        observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                        nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                        estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                        motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                    )
                                                                }

                                                                finalizarCarga()
                                                            },
                                                            onError = { mensaje ->
                                                                registrarError(
                                                                    "Intervenciones: $mensaje"
                                                                )
                                                            }
                                                        )
                                                    }

                                                    if (activos.isEmpty()) {
                                                        ImportadorActivos.cargarActivos(
                                                            onFinalizado = { datos ->
                                                                activos = datos.map { activo ->
                                                                    ActivoResumen(
                                                                        codigo = activo["codigo"]?.toString().orEmpty(),
                                                                        subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                        tipo = activo["tipo"]?.toString().orEmpty(),
                                                                        marca = activo["marca"]?.toString().orEmpty(),
                                                                        modelo = activo["modelo"]?.toString().orEmpty(),
                                                                        indicador = activo["indicador"]?.toString().orEmpty(),
                                                                        horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                        kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                        ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                        status = activo["status"]?.toString().orEmpty()
                                                                    )
                                                                }.sortedBy { it.codigo }

                                                                cargarCentroBorradores()
                                                            },
                                                            onError = { mensaje ->
                                                                cargandoBorradores = false
                                                                mensajeBorradores = mensaje
                                                                pantallaActual = "MIS_BORRADORES"
                                                            }
                                                        )
                                                    } else {
                                                        cargarCentroBorradores()
                                                    }
                                                }

                                                "Mi historial" -> {
                                                    cargandoHistorial = true
                                                    mensajeHistorial = ""
                                                    historial = emptyList()
                                                    historialHuella = emptyList()
                                                    historialIntervencion = emptyList()

                                                    var cargasPendientes = 3

                                                    fun finalizarHistorial() {
                                                        cargasPendientes--

                                                        if (cargasPendientes <= 0) {
                                                            cargandoHistorial = false
                                                            pantallaActual = "MI_HISTORIAL"
                                                        }
                                                    }

                                                    fun registrarErrorHistorial(mensaje: String) {
                                                        mensajeHistorial =
                                                            if (mensajeHistorial.isBlank()) {
                                                                mensaje
                                                            } else {
                                                                mensajeHistorial + "\n" + mensaje
                                                            }

                                                        finalizarHistorial()
                                                    }

                                                    MantenimientoRepository.cargarMiHistorial(
                                                        onFinalizado = { datos ->
                                                            historial = datos.map { registro ->
                                                                RegistroHistorial(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    tipoServicio = registro["tipoServicio"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    accionEjecutada = registro["accionEjecutada"]?.toString().orEmpty(),
                                                                    observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                    ordenTrabajo = registro["ordenTrabajo"]?.toString().orEmpty(),
                                                                    numeroPedido = registro["numeroPedido"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                    motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                )
                                                            }

                                                            finalizarHistorial()
                                                        },
                                                        onError = { mensaje ->
                                                            registrarErrorHistorial(
                                                                "Mantenimiento: $mensaje"
                                                            )
                                                        }
                                                    )

                                                    VulcanizacionRepository.cargarMiHistorial(
                                                        onFinalizado = { datos ->
                                                            historialHuella = datos.map { registro ->
                                                                RegistroHistorialHuella(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    huellas = (1..12).map { posicion ->
                                                                        (registro["P$posicion"] as? Number)?.toDouble()
                                                                    },
                                                                    estadoGeneral = registro["estadoGeneral"]?.toString().orEmpty(),
                                                                    novedad = registro["novedad"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                    motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                )
                                                            }

                                                            finalizarHistorial()
                                                        },
                                                        onError = { mensaje ->
                                                            registrarErrorHistorial(
                                                                "Toma de huella: $mensaje"
                                                            )
                                                        }
                                                    )

                                                    VulcanizacionRepository.cargarMiHistorialIntervencion(
                                                        onFinalizado = { datos ->
                                                            historialIntervencion = datos.map { registro ->
                                                                RegistroHistorialIntervencion(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    tipoIntervencion = registro["tipoIntervencion"]?.toString().orEmpty(),
                                                                    posicion = registro["posicion"]?.toString().orEmpty(),
                                                                    huella = (registro["huella"] as? Number)?.toDouble(),
                                                                    marcaLlanta = registro["marcaLlanta"]?.toString().orEmpty(),
                                                                    medidaLlanta = registro["medidaLlanta"]?.toString().orEmpty(),
                                                                    serieLlanta = registro["serieLlanta"]?.toString().orEmpty(),
                                                                    motivo = registro["motivo"]?.toString().orEmpty(),
                                                                    observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty(),
                                                                    motivoDevolucion = registro["motivoDevolucion"]?.toString().orEmpty()
                                                                )
                                                            }

                                                            finalizarHistorial()
                                                        },
                                                        onError = { mensaje ->
                                                            registrarErrorHistorial(
                                                                "Intervenciones: $mensaje"
                                                            )
                                                        }
                                                    )
                                                }

                                                "Revisión de registros" -> {
                                                    cargandoRevision = true
                                                    mensajeRevision = ""
                                                    registrosRevision = emptyList()
                                                    registrosRevisionHuella = emptyList()
                                                    registrosRevisionIntervencion = emptyList()

                                                    var cargasPendientes = 3

                                                    fun finalizarRevision() {
                                                        cargasPendientes--
                                                        if (cargasPendientes <= 0) {
                                                            cargandoRevision = false
                                                            pantallaActual = "REVISION_REGISTROS"
                                                        }
                                                    }

                                                    fun registrarErrorRevision(mensaje: String) {
                                                        mensajeRevision =
                                                            if (mensajeRevision.isBlank()) {
                                                                mensaje
                                                            } else {
                                                                mensajeRevision + "\n" + mensaje
                                                            }
                                                        finalizarRevision()
                                                    }

                                                    MantenimientoRepository.cargarRegistrosPendientesRevision(
                                                        onFinalizado = { datos ->
                                                            registrosRevision = datos.map { registro ->
                                                                RegistroRevision(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    tipoServicio = registro["tipoServicio"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    accionEjecutada = registro["accionEjecutada"]?.toString().orEmpty(),
                                                                    observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                    ordenTrabajo = registro["ordenTrabajo"]?.toString().orEmpty(),
                                                                    numeroPedido = registro["numeroPedido"]?.toString().orEmpty(),
                                                                    uidUsuario = registro["uidUsuario"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty()
                                                                )
                                                            }
                                                            finalizarRevision()
                                                        },
                                                        onError = { mensaje ->
                                                            registrarErrorRevision("Mantenimiento: $mensaje")
                                                        }
                                                    )

                                                    VulcanizacionRepository.cargarRegistrosPendientesRevision(
                                                        onFinalizado = { datos ->
                                                            registrosRevisionHuella = datos.map { registro ->
                                                                RegistroRevisionHuella(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    huellas = (1..12).map { posicion ->
                                                                        (registro["P$posicion"] as? Number)?.toDouble()
                                                                    },
                                                                    estadoGeneral = registro["estadoGeneral"]?.toString().orEmpty(),
                                                                    novedad = registro["novedad"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    uidUsuario = registro["uidUsuario"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty()
                                                                )
                                                            }
                                                            finalizarRevision()
                                                        },
                                                        onError = { mensaje ->
                                                            registrarErrorRevision("Toma de huella: $mensaje")
                                                        }
                                                    )

                                                    VulcanizacionRepository.cargarIntervencionesPendientesRevision(
                                                        onFinalizado = { datos ->
                                                            registrosRevisionIntervencion = datos.map { registro ->
                                                                RegistroRevisionIntervencion(
                                                                    id = registro["id"]?.toString().orEmpty(),
                                                                    codigoActivo = registro["codigoActivo"]?.toString().orEmpty(),
                                                                    proyecto = registro["proyecto"]?.toString().orEmpty(),
                                                                    kilometraje = (registro["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (registro["horometro"] as? Number)?.toDouble(),
                                                                    tipoIntervencion = registro["tipoIntervencion"]?.toString().orEmpty(),
                                                                    posicion = registro["posicion"]?.toString().orEmpty(),
                                                                    huella = (registro["huella"] as? Number)?.toDouble(),
                                                                    marcaLlanta = registro["marcaLlanta"]?.toString().orEmpty(),
                                                                    medidaLlanta = registro["medidaLlanta"]?.toString().orEmpty(),
                                                                    serieLlanta = registro["serieLlanta"]?.toString().orEmpty(),
                                                                    motivo = registro["motivo"]?.toString().orEmpty(),
                                                                    observaciones = registro["observaciones"]?.toString().orEmpty(),
                                                                    nombreTecnico = registro["nombreTecnico"]?.toString().orEmpty(),
                                                                    uidUsuario = registro["uidUsuario"]?.toString().orEmpty(),
                                                                    estadoRegistro = registro["estadoRegistro"]?.toString().orEmpty()
                                                                )
                                                            }
                                                            finalizarRevision()
                                                        },
                                                        onError = { mensaje ->
                                                            registrarErrorRevision("Intervenciones: $mensaje")
                                                        }
                                                    )
                                                }                                                "Reportes" -> {
                                                cargandoReportes = true
                                                mensajeReportes = "Cargando indicadores..."
                                                resumenReportes = null

                                                reporteCompleto = null

                                                ReportesRepository.cargarReporteCompleto(
                                                    onFinalizado = { reporte ->
                                                        reporteBase = reporte
                                                        reporteCompleto = reporte
                                                        resumenReportes = reporte.resumen
                                                        cargandoReportes = false
                                                        mensajeReportes = ""
                                                        pantallaActual = "REPORTES"
                                                    },

                                                    onError = { mensaje ->
                                                        cargandoReportes = false
                                                        mensajeReportes = mensaje
                                                        pantallaActual = "REPORTES"
                                                    }
                                                )
                                            }

                                            }
                                        },
                                        onCerrarSesion = {
                                            FirebaseAuth.getInstance().signOut()
                                            perfilUsuario = null
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PerfilUsuario(
    val uid: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val cargo: String,
    val rol: String,
    val estadoUsuario: String
)

@Composable
fun LoginScreen(
    onLoginSuccess: (PerfilUsuario) -> Unit
) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var mostrarPassword by remember {
        mutableStateOf(false)
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    fun iniciarSesion() {
        if (email.isBlank() || password.isBlank()) {
            mensaje = "Ingresa el correo y la contraseña."
            return
        }

        cargando = true
        mensaje = ""

        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).addOnCompleteListener { tarea ->

            if (!tarea.isSuccessful) {
                cargando = false
                mensaje = "Correo o contraseña incorrectos."
                return@addOnCompleteListener
            }

            val uid = auth.currentUser?.uid

            if (uid == null) {
                cargando = false
                mensaje = "No fue posible identificar al usuario."
                auth.signOut()
                return@addOnCompleteListener
            }

            firestore.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { documento ->

                    if (!documento.exists()) {
                        cargando = false
                        mensaje = "El usuario no tiene un perfil registrado."
                        auth.signOut()
                        return@addOnSuccessListener
                    }

                    val estadoUsuario =
                        documento.getString("estadoUsuario").orEmpty()

                    if (estadoUsuario != "ACTIVO") {
                        cargando = false
                        mensaje = "El usuario se encuentra inactivo."
                        auth.signOut()
                        return@addOnSuccessListener
                    }

                    val perfil = PerfilUsuario(
                        uid = uid,
                        nombres = documento.getString("nombres").orEmpty(),
                        apellidos = documento.getString("apellidos").orEmpty(),
                        email = documento.getString("email").orEmpty(),
                        cargo = documento.getString("cargo").orEmpty(),
                        rol = documento.getString("rol").orEmpty(),
                        estadoUsuario = estadoUsuario
                    )

                    cargando = false
                    onLoginSuccess(perfil)
                }
                .addOnFailureListener {
                    cargando = false
                    mensaje = "No se pudo consultar el perfil del usuario."
                    auth.signOut()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MEV Mantenimiento",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestión de mantenimiento de flota",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Correo electrónico")
            },
            singleLine = true,
            enabled = !cargando,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Contraseña")
            },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = if (mostrarPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        mostrarPassword = !mostrarPassword
                    }
                ) {
                    Text(
                        text = if (mostrarPassword) {
                            "Ocultar"
                        } else {
                            "Mostrar"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    iniciarSesion()
                }
            )
        )

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                iniciarSesion()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar sesión")
            }
        }
    }
}

@Composable
fun HomeScreen(
    perfil: PerfilUsuario,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido, ${perfil.nombres}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = perfil.cargo,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rol: ${perfil.rol}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout
        ) {
            Text("Cerrar sesión")
        }
    }
}