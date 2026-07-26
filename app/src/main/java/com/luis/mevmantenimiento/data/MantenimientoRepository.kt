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
            "kilometraje" to convertirNumero(kilometraje),
            "horometro" to convertirNumero(horometro),
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

    fun cargarMisBorradores(
        onFinalizado: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("registros_mantenimiento")
            .whereEqualTo("uidUsuario", usuarioActual.uid)
            .whereEqualTo("estadoRegistro", "BORRADOR")
            .get()
            .addOnSuccessListener { resultado ->

                val lista = resultado.documents.map { documento ->
                    documento.data.orEmpty().toMutableMap().apply {
                        this["id"] = documento.id
                    }
                }

                onFinalizado(lista)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudieron cargar los borradores: ${error.message}"
                )
            }
    }

    fun actualizarBorrador(
        idRegistro: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        if (idRegistro.isBlank()) {
            onError("No se encontró el identificador del borrador.")
            return
        }

        val cambios = mapOf<String, Any?>(
            "tipoServicio" to tipoServicio.trim().uppercase(),
            "kilometraje" to convertirNumero(kilometraje),
            "horometro" to convertirNumero(horometro),
            "accionEjecutada" to accionEjecutada.trim(),
            "observaciones" to observaciones.trim(),
            "ordenTrabajo" to ordenTrabajo.trim(),
            "numeroPedido" to numeroPedido.trim(),
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("registros_mantenimiento")
            .document(idRegistro)
            .update(cambios)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo actualizar el borrador: ${error.message}"
                )
            }
    }

    fun enviarBorrador(
        idRegistro: String,
        tipoServicio: String,
        kilometraje: String,
        horometro: String,
        accionEjecutada: String,
        observaciones: String,
        ordenTrabajo: String,
        numeroPedido: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        if (idRegistro.isBlank()) {
            onError("No se encontró el identificador del borrador.")
            return
        }

        if (accionEjecutada.isBlank()) {
            onError("Debes ingresar la acción ejecutada.")
            return
        }

        val cambios = mapOf<String, Any?>(
            "tipoServicio" to tipoServicio.trim().uppercase(),
            "kilometraje" to convertirNumero(kilometraje),
            "horometro" to convertirNumero(horometro),
            "accionEjecutada" to accionEjecutada.trim(),
            "observaciones" to observaciones.trim(),
            "ordenTrabajo" to ordenTrabajo.trim(),
            "numeroPedido" to numeroPedido.trim(),
            "estadoRegistro" to "ENVIADO",
            "fechaActualizacion" to FieldValue.serverTimestamp(),
            "fechaEnvio" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("registros_mantenimiento")
            .document(idRegistro)
            .update(cambios)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo enviar el borrador: ${error.message}"
                )
            }
    }

    private fun convertirNumero(
        valor: String
    ): Double? {
        return valor
            .trim()
            .replace(",", ".")
            .toDoubleOrNull()
    }
}