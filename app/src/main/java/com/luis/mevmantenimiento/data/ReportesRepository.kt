package com.luis.mevmantenimiento.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.luis.mevmantenimiento.ui.screens.ReporteResumen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReportesRepository {

    fun cargarResumen(
        onFinalizado: (ReporteResumen) -> Unit,
        onError: (String) -> Unit
    ) {
        cargarReporteCompleto(
            onFinalizado = { reporte ->
                onFinalizado(reporte.resumen)
            },
            onError = onError
        )
    }

    fun cargarReporteCompleto(
        onFinalizado: (ReporteDatos) -> Unit,
        onError: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        var mantenimientos: List<Map<String, Any?>>? = null
        var huellas: List<Map<String, Any?>>? = null
        var intervenciones: List<Map<String, Any?>>? = null
        var errorReportado = false

        fun intentarFinalizar() {
            if (
                mantenimientos != null &&
                huellas != null &&
                intervenciones != null &&
                !errorReportado
            ) {
                val listaMantenimientos = mantenimientos.orEmpty()
                val listaHuellas = huellas.orEmpty()
                val listaIntervenciones = intervenciones.orEmpty()

                onFinalizado(
                    ReporteDatos(
                        resumen = construirResumen(
                            mantenimientos = listaMantenimientos,
                            huellas = listaHuellas,
                            intervenciones = listaIntervenciones
                        ),
                        mantenimientos = listaMantenimientos,
                        huellas = listaHuellas,
                        intervenciones = listaIntervenciones
                    )
                )
            }
        }

        fun reportarError(mensaje: String) {
            if (!errorReportado) {
                errorReportado = true
                onError(mensaje)
            }
        }

        db.collection("registros_mantenimiento")
            .get()
            .addOnSuccessListener { resultado ->
                mantenimientos = resultado.documents.map { documento ->
                    documento.data
                        .orEmpty()
                        .toMutableMap()
                        .apply {
                            this["id"] = documento.id
                        }
                }
                intentarFinalizar()
            }
            .addOnFailureListener { error ->
                reportarError(
                    "No se pudieron cargar los mantenimientos: ${error.message}"
                )
            }

        db.collection("tomas_huella")
            .get()
            .addOnSuccessListener { resultado ->
                huellas = resultado.documents.map { documento ->
                    documento.data
                        .orEmpty()
                        .toMutableMap()
                        .apply {
                            this["id"] = documento.id
                        }
                }
                intentarFinalizar()
            }
            .addOnFailureListener { error ->
                reportarError(
                    "No se pudieron cargar las tomas de huella: ${error.message}"
                )
            }

        db.collection("intervenciones_llanta")
            .get()
            .addOnSuccessListener { resultado ->
                intervenciones = resultado.documents.map { documento ->
                    documento.data
                        .orEmpty()
                        .toMutableMap()
                        .apply {
                            this["id"] = documento.id
                        }
                }
                intentarFinalizar()
            }
            .addOnFailureListener { error ->
                reportarError(
                    "No se pudieron cargar las intervenciones: ${error.message}"
                )
            }
    }

    fun aplicarFiltros(
        reporteOriginal: ReporteDatos,
        filtros: FiltrosReporte
    ): ReporteDatos {
        val fechaDesde = parsearFecha(
            filtros.fechaDesde,
            finDelDia = false
        )

        val fechaHasta = parsearFecha(
            filtros.fechaHasta,
            finDelDia = true
        )

        val mantenimientos = reporteOriginal.mantenimientos.filter { registro ->
            coincideRegistro(
                registro = registro,
                filtros = filtros,
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta
            )
        }

        val huellas = reporteOriginal.huellas.filter { registro ->
            coincideRegistro(
                registro = registro,
                filtros = filtros,
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta
            )
        }

        val intervenciones = reporteOriginal.intervenciones.filter { registro ->
            coincideRegistro(
                registro = registro,
                filtros = filtros,
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta
            )
        }

        return ReporteDatos(
            resumen = construirResumen(
                mantenimientos = mantenimientos,
                huellas = huellas,
                intervenciones = intervenciones
            ),
            mantenimientos = mantenimientos,
            huellas = huellas,
            intervenciones = intervenciones
        )
    }

    private fun coincideRegistro(
        registro: Map<String, Any?>,
        filtros: FiltrosReporte,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): Boolean {
        val proyecto = texto(registro["proyecto"])
        val activo = texto(registro["codigoActivo"])
        val tecnico = (
                texto(registro["nombreTecnico"]) + " " +
                        texto(registro["emailUsuario"])
                ).trim()
        val estado = texto(registro["estadoRegistro"])

        if (
            filtros.proyecto.isNotBlank() &&
            !proyecto.contains(
                filtros.proyecto.trim(),
                ignoreCase = true
            )
        ) {
            return false
        }

        if (
            filtros.activo.isNotBlank() &&
            !activo.contains(
                filtros.activo.trim(),
                ignoreCase = true
            )
        ) {
            return false
        }

        if (
            filtros.tecnico.isNotBlank() &&
            !tecnico.contains(
                filtros.tecnico.trim(),
                ignoreCase = true
            )
        ) {
            return false
        }

        if (
            filtros.estado.isNotBlank() &&
            !estado.equals(
                filtros.estado.trim(),
                ignoreCase = true
            )
        ) {
            return false
        }

        if (fechaDesde != null || fechaHasta != null) {
            val fechaRegistro =
                (registro["fechaCreacion"] as? Timestamp)
                    ?.toDate()
                    ?.time
                    ?: return false

            if (
                fechaDesde != null &&
                fechaRegistro < fechaDesde
            ) {
                return false
            }

            if (
                fechaHasta != null &&
                fechaRegistro > fechaHasta
            ) {
                return false
            }
        }

        return true
    }

    private fun parsearFecha(
        valor: String,
        finDelDia: Boolean
    ): Long? {
        val limpio = valor.trim()

        if (limpio.isBlank()) {
            return null
        }

        return try {
            val formato = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).apply {
                isLenient = false
            }

            val fecha = formato.parse(limpio)
                ?: return null

            val calendario = Calendar.getInstance().apply {
                time = fecha

                if (finDelDia) {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                } else {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            calendario.timeInMillis
        } catch (_: Exception) {
            null
        }
    }

    private fun construirResumen(
        mantenimientos: List<Map<String, Any?>>,
        huellas: List<Map<String, Any?>>,
        intervenciones: List<Map<String, Any?>>
    ): ReporteResumen {

        fun estado(registro: Map<String, Any?>): String {
            return registro["estadoRegistro"]
                ?.toString()
                .orEmpty()
                .uppercase()
        }

        fun tipoServicio(registro: Map<String, Any?>): String {
            return registro["tipoServicio"]
                ?.toString()
                .orEmpty()
                .uppercase()
        }

        fun tipoIntervencion(registro: Map<String, Any?>): String {
            return registro["tipoIntervencion"]
                ?.toString()
                .orEmpty()
                .uppercase()
        }

        val huellasCriticas = huellas.sumOf { registro ->
            (1..12).count { posicion ->
                val valor =
                    (registro["P$posicion"] as? Number)?.toDouble()

                valor != null &&
                        valor > 0.0 &&
                        valor <= 6.0
            }
        }

        val pendientesRevision =
            mantenimientos.count { estado(it) == "ENVIADO" } +
                    huellas.count { estado(it) == "ENVIADO" } +
                    intervenciones.count { estado(it) == "ENVIADO" }

        return ReporteResumen(
            totalMantenimientos = mantenimientos.size,
            preventivos = mantenimientos.count {
                tipoServicio(it).contains("PREVENTIVO")
            },
            correctivos = mantenimientos.count {
                tipoServicio(it).contains("CORRECTIVO")
            },
            mantenimientosEnviados = mantenimientos.count {
                estado(it) == "ENVIADO"
            },
            mantenimientosAprobados = mantenimientos.count {
                estado(it) == "APROBADO"
            },
            mantenimientosDevueltos = mantenimientos.count {
                estado(it) == "DEVUELTO"
            },

            totalHuellas = huellas.size,
            huellasCriticas = huellasCriticas,

            totalIntervenciones = intervenciones.size,
            cambios = intervenciones.count {
                tipoIntervencion(it).contains("CAMBIO")
            },
            rotaciones = intervenciones.count {
                tipoIntervencion(it).contains("ROTACION") ||
                        tipoIntervencion(it).contains("ROTACIÓN")
            },
            reparaciones = intervenciones.count {
                tipoIntervencion(it).contains("REPARACION") ||
                        tipoIntervencion(it).contains("REPARACIÓN")
            },
            montajes = intervenciones.count {
                tipoIntervencion(it).contains("MONTAJE")
            },
            desmontajes = intervenciones.count {
                tipoIntervencion(it).contains("DESMONTAJE")
            },
            bajas = intervenciones.count {
                tipoIntervencion(it).contains("BAJA")
            },

            registrosPendientesRevision = pendientesRevision
        )
    }

    private fun texto(
        valor: Any?
    ): String {
        return valor
            ?.toString()
            .orEmpty()
    }
}