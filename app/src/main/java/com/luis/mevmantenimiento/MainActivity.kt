package com.luis.mevmantenimiento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                                        registros = registrosRevision,
                                        cargando = cargandoRevision,
                                        mensaje = mensajeRevision,

                                        onAprobar = { registro ->
                                            if (!cargandoRevision) {
                                                cargandoRevision = true
                                                mensajeRevision = "Aprobando registro..."

                                                MantenimientoRepository.actualizarEstadoRevision(
                                                    idRegistro = registro.id,
                                                    nuevoEstado = "APROBADO",

                                                    onFinalizado = {
                                                        registrosRevision =
                                                            registrosRevision.filter {
                                                                it.id != registro.id
                                                            }

                                                        cargandoRevision = false
                                                        mensajeRevision =
                                                            "Registro ${registro.codigoActivo} " +
                                                                    "aprobado correctamente."
                                                    },

                                                    onError = { mensaje ->
                                                        cargandoRevision = false
                                                        mensajeRevision = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onDevolver = { registro, motivoDevolucion ->
                                            if (!cargandoRevision) {
                                                cargandoRevision = true
                                                mensajeRevision = "Devolviendo registro..."

                                                MantenimientoRepository.devolverRegistro(
                                                    idRegistro = registro.id,
                                                    motivoDevolucion = motivoDevolucion,

                                                    onFinalizado = {
                                                        registrosRevision =
                                                            registrosRevision.filter {
                                                                it.id != registro.id
                                                            }

                                                        cargandoRevision = false
                                                        mensajeRevision =
                                                            "Registro ${registro.codigoActivo} " +
                                                                    "devuelto para corrección."
                                                    },

                                                    onError = { mensaje ->
                                                        cargandoRevision = false
                                                        mensajeRevision = mensaje
                                                    }
                                                )
                                            }
                                        },

                                        onVolver = {
                                            mensajeRevision = ""
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }
                                "MI_HISTORIAL" -> {
                                    MiHistorialScreen(
                                        registros = historial,
                                        cargando = cargandoHistorial,
                                        mensaje = mensajeHistorial,
                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                "MIS_BORRADORES" -> {
                                    MisBorradoresScreen(
                                        borradores = borradores,
                                        cargando = cargandoBorradores,
                                        mensaje = mensajeBorradores,
                                        onSeleccionarBorrador = { borrador ->
                                            borradorSeleccionado = borrador
                                            pantallaActual = "EDITAR_BORRADOR"
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

                                                    MantenimientoRepository.cargarMisBorradores(
                                                        onFinalizado = { datos ->
                                                            borradores = datos.map { borrador ->
                                                                BorradorMantenimiento(
                                                                    id = borrador["id"]?.toString().orEmpty(),
                                                                    codigoActivo = borrador["codigoActivo"]?.toString().orEmpty(),
                                                                    tipoServicio = borrador["tipoServicio"]?.toString().orEmpty(),
                                                                    kilometraje = (borrador["kilometraje"] as? Number)?.toDouble(),
                                                                    horometro = (borrador["horometro"] as? Number)?.toDouble(),
                                                                    accionEjecutada = borrador["accionEjecutada"]?.toString().orEmpty(),
                                                                    observaciones = borrador["observaciones"]?.toString().orEmpty(),
                                                                    ordenTrabajo = borrador["ordenTrabajo"]?.toString().orEmpty(),
                                                                    numeroPedido = borrador["numeroPedido"]?.toString().orEmpty(),
                                                                    estadoRegistro = borrador["estadoRegistro"]?.toString().orEmpty(),
                                                                    motivoDevolucion = borrador["motivoDevolucion"]?.toString().orEmpty()
                                                                )
                                                            }

                                                            cargandoBorradores = false
                                                            pantallaActual = "MIS_BORRADORES"
                                                        },

                                                        onError = { mensaje ->
                                                            cargandoBorradores = false
                                                            mensajeBorradores = mensaje
                                                            pantallaActual = "MIS_BORRADORES"
                                                        }
                                                    )
                                                }

                                                "Mi historial" -> {
                                                    cargandoHistorial = true
                                                    mensajeHistorial = ""
                                                    historial = emptyList()

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

                                                            cargandoHistorial = false
                                                            pantallaActual = "MI_HISTORIAL"
                                                        },

                                                        onError = { mensaje ->
                                                            cargandoHistorial = false
                                                            mensajeHistorial = mensaje
                                                            pantallaActual = "MI_HISTORIAL"
                                                        }
                                                    )
                                                }

                                                "Revisión de registros" -> {
                                                    cargandoRevision = true
                                                    mensajeRevision = ""
                                                    registrosRevision = emptyList()

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

                                                            cargandoRevision = false
                                                            pantallaActual = "REVISION_REGISTROS"
                                                        },

                                                        onError = { mensaje ->
                                                            cargandoRevision = false
                                                            mensajeRevision = mensaje
                                                            pantallaActual = "REVISION_REGISTROS"
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