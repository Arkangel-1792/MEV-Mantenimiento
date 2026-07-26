package com.luis.mevmantenimiento.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object MantenimientoRepository {

    fun guardarRegistro(
        codigoActivo: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String,
        estadoRegistro: String,
        onFinalizado: (idRegistro: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        val codigoNormalizado = codigoActivo.trim().uppercase()

        if (codigoNormalizado.isBlank()) {
            onError("Debes seleccionar un activo.")
            return
        }

        if (
            estadoRegistro == "ENVIADO" &&
            accionEjecutada.isBlank()
        ) {
            onError("Debes ingresar la acción ejecutada.")
            return
        }

        val datos = hashMapOf<String, Any?>(
            "codigoActivo" to codigoNormalizado,
            "tipoServicio" to tipoServicio.trim().uppercase(),
            "kilometraje" to kilometraje
                .trim()
                .replace(",", ".")
                .toDoubleOrNull(),
            "horometro" to horometro
                .trim()
                .replace(",", ".")
                .toDoubleOrNull(),
            "accionEjecutada" to accionEjecutada.trim(),
            "observaciones" to observaciones.trim(),
            "ordenTrabajo" to ordenTrabajo.trim(),
            "numeroPedido" to numeroPedido.trim(),
            "estadoRegistro" to estadoRegistro,
            "uidUsuario" to usuarioActual.uid,
            "emailUsuario" to usuarioActual.email.orEmpty(),
            "fechaCreacion" to FieldValue.serverTimestamp(),
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("registros_mantenimiento")
            .add(datos)
            .addOnSuccessListener { documento ->
                onFinalizado(documento.id)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo guardar el mantenimiento: ${error.message}"
                )
            }
    }
}