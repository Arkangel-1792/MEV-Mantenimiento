package com.luis.mevmantenimiento.data

import com.google.firebase.firestore.FirebaseFirestore
import com.luis.mevmantenimiento.ui.screens.ReporteResumen

object ReportesRepository {

    fun cargarResumen(
        onFinalizado: (ReporteResumen) -> Unit,
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
                onFinalizado(
                    construirResumen(
                        mantenimientos = mantenimientos.orEmpty(),
                        huellas = huellas.orEmpty(),
                        intervenciones = intervenciones.orEmpty()
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
                    documento.data.orEmpty()
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
                    documento.data.orEmpty()
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
                    documento.data.orEmpty()
                }
                intentarFinalizar()
            }
            .addOnFailureListener { error ->
                reportarError(
                    "No se pudieron cargar las intervenciones: ${error.message}"
                )
            }
    }

    private fun construirResumen(
        mantenimientos: List<Map<String, Any?>>,
        huellas: List<Map<String, Any?>>,
        intervenciones: List<Map<String, Any?>>
    ): ReporteResumen {

        fun estado(
            registro: Map<String, Any?>
        ): String {
            return registro["estadoRegistro"]
                ?.toString()
                .orEmpty()
                .uppercase()
        }

        fun tipoServicio(
            registro: Map<String, Any?>
        ): String {
            return registro["tipoServicio"]
                ?.toString()
                .orEmpty()
                .uppercase()
        }

        fun tipoIntervencion(
            registro: Map<String, Any?>
        ): String {
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
}