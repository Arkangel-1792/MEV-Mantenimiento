package com.luis.mevmantenimiento.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.luis.mevmantenimiento.ui.screens.ActivoResumen

object ActivosRepository {

    private const val COLECCION_ACTIVOS = "activos"

    fun guardarActivo(
        activo: ActivoResumen,
        esNuevo: Boolean,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val codigo = activo.codigo.trim().uppercase()

        if (codigo.isBlank()) {
            onError("El código del activo es obligatorio.")
            return
        }

        if (activo.subtipo.isBlank() || activo.tipo.isBlank()) {
            onError("Debes indicar el tipo y subtipo del activo.")
            return
        }

        if (
            activo.aplicaVulcanizacion &&
            activo.cantidadPosiciones !in 1..12
        ) {
            onError("La cantidad de posiciones debe estar entre 1 y 12.")
            return
        }

        if (
            activo.indicador.isNotBlank() &&
            activo.indicador.uppercase() !in setOf("HR", "KM", "AMBOS")
        ) {
            onError("El indicador debe ser HR, KM o AMBOS.")
            return
        }

        val documento = FirebaseFirestore.getInstance()
            .collection(COLECCION_ACTIVOS)
            .document(codigo)

        val guardar = {
            val datos = datosActivo(activo).toMutableMap()

            datos["fechaActualizacion"] = FieldValue.serverTimestamp()
            if (esNuevo) {
                datos["fechaCreacion"] = FieldValue.serverTimestamp()
            }

            documento.set(datos, SetOptions.merge())
                .addOnSuccessListener {
                    onFinalizado()
                }
                .addOnFailureListener { error ->
                    onError(
                        "No se pudo guardar el activo: ${error.message}"
                    )
                }
        }

        if (!esNuevo) {
            guardar()
            return
        }

        documento.get()
            .addOnSuccessListener { existente ->
                if (existente.exists()) {
                    onError("Ya existe un activo con el código $codigo.")
                } else {
                    guardar()
                }
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo validar el código del activo: ${error.message}"
                )
            }
    }

    private fun datosActivo(
        activo: ActivoResumen
    ): Map<String, Any?> {
        return mapOf(
            "codigo" to activo.codigo.trim().uppercase(),
            "subtipo" to activo.subtipo.trim().uppercase(),
            "tipo" to activo.tipo.trim().uppercase(),
            "marca" to activo.marca.trim().uppercase(),
            "modelo" to activo.modelo.trim(),
            "serie" to activo.serie.trim().uppercase(),
            "anioFabricacion" to activo.anioFabricacion,
            "matricula" to activo.matricula.trim().uppercase(),
            "indicador" to activo.indicador.trim().uppercase(),
            "intervalo" to activo.intervalo,
            "horometro" to activo.horometro,
            "kilometraje" to activo.kilometraje,
            "ubicacionActual" to activo.ubicacionActual.trim(),
            "status" to activo.status.trim().uppercase(),
            "telemetria" to activo.telemetria.trim().uppercase(),
            "usaHorometro" to activo.usaHorometro,
            "usaKilometraje" to activo.usaKilometraje,
            "aplicaVulcanizacion" to activo.aplicaVulcanizacion,
            "cantidadPosiciones" to
                    if (activo.aplicaVulcanizacion) {
                        activo.cantidadPosiciones
                    } else {
                        0
                    },
            "configuracionRuedas" to
                    activo.configuracionRuedas.trim().uppercase(),
            "estadoConfiguracion" to
                    activo.estadoConfiguracion.trim().uppercase(),
            "permitePreventivo" to activo.permitePreventivo,
            "permiteCorrectivo" to activo.permiteCorrectivo,
            "permiteTomaHuella" to
                    (activo.aplicaVulcanizacion && activo.permiteTomaHuella),
            "permiteIntervencionLlanta" to
                    (activo.aplicaVulcanizacion &&
                            activo.permiteIntervencionLlanta)
        )
    }
}
