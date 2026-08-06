package com.luis.mevmantenimiento.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object MantenimientoRepository {

    private const val COLECCION_REGISTROS =
        "registros_mantenimiento"

    private const val ESTADO_BORRADOR = "BORRADOR"
    private const val ESTADO_ENVIADO = "ENVIADO"
    private const val ESTADO_APROBADO = "APROBADO"
    private const val ESTADO_DEVUELTO = "DEVUELTO"

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
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        val codigoNormalizado =
            codigoActivo.trim().uppercase()

        val estadoNormalizado =
            estadoRegistro.trim().uppercase()

        if (codigoNormalizado.isBlank()) {
            onError("Debes seleccionar un activo.")
            return
        }

        if (
            estadoNormalizado != ESTADO_BORRADOR &&
            estadoNormalizado != ESTADO_ENVIADO
        ) {
            onError("El estado del registro no es válido.")
            return
        }

        if (
            estadoNormalizado == ESTADO_ENVIADO &&
            accionEjecutada.isBlank()
        ) {
            onError("Debes ingresar la acción ejecutada.")
            return
        }

        val datos = hashMapOf<String, Any?>(
            "codigoActivo" to codigoNormalizado,
            "tipoServicio" to
                    tipoServicio.trim().uppercase(),
            "kilometraje" to
                    convertirNumero(kilometraje),
            "horometro" to
                    convertirNumero(horometro),
            "accionEjecutada" to
                    accionEjecutada.trim(),
            "observaciones" to
                    observaciones.trim(),
            "ordenTrabajo" to
                    ordenTrabajo.trim(),
            "numeroPedido" to
                    numeroPedido.trim(),
            "estadoRegistro" to estadoNormalizado,
            "uidUsuario" to usuarioActual.uid,
            "emailUsuario" to
                    usuarioActual.email.orEmpty(),
            "fechaCreacion" to
                    FieldValue.serverTimestamp(),
            "fechaActualizacion" to
                    FieldValue.serverTimestamp()
        )

        if (estadoNormalizado == ESTADO_ENVIADO) {
            datos["fechaEnvio"] =
                FieldValue.serverTimestamp()
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .add(datos)
            .addOnSuccessListener { documento ->
                onFinalizado(documento.id)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo guardar el mantenimiento: " +
                            "${error.message}"
                )
            }
    }

    /**
     * Carga los registros que el usuario puede corregir:
     * BORRADOR y DEVUELTO.
     */
    fun cargarMisBorradores(
        onFinalizado: (
            List<Map<String, Any?>>
        ) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .whereEqualTo(
                "uidUsuario",
                usuarioActual.uid
            )
            .whereIn(
                "estadoRegistro",
                listOf(
                    ESTADO_BORRADOR,
                    ESTADO_DEVUELTO
                )
            )
            .get()
            .addOnSuccessListener { resultado ->
                val lista =
                    resultado.documents.map { documento ->
                        documento.data
                            .orEmpty()
                            .toMutableMap()
                            .apply {
                                this["id"] = documento.id
                            }
                    }

                onFinalizado(lista)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudieron cargar los registros " +
                            "pendientes de corrección: " +
                            "${error.message}"
                )
            }
    }

    /**
     * Muestra los registros enviados que todavía están
     * pendientes de revisión.
     */
    fun cargarMiHistorial(
        onFinalizado: (
            List<Map<String, Any?>>
        ) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .whereEqualTo(
                "uidUsuario",
                usuarioActual.uid
            )
            .whereIn(
            "estadoRegistro",
            listOf(
                ESTADO_ENVIADO,
                ESTADO_DEVUELTO,
                ESTADO_APROBADO
                  )
            )
            .get()
            .addOnSuccessListener { resultado ->
                val lista =
                    resultado.documents.map { documento ->
                        documento.data
                            .orEmpty()
                            .toMutableMap()
                            .apply {
                                this["id"] = documento.id
                            }
                    }

                onFinalizado(lista)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo cargar el historial: " +
                            "${error.message}"
                )
            }
    }

    /**
     * Carga para el revisor todos los registros
     * que se encuentran en estado ENVIADO.
     */
    fun cargarRegistrosPendientesRevision(
        onFinalizado: (
            List<Map<String, Any?>>
        ) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .whereEqualTo(
                "estadoRegistro",
                ESTADO_ENVIADO
            )
            .get()
            .addOnSuccessListener { resultado ->
                val lista =
                    resultado.documents.map { documento ->
                        documento.data
                            .orEmpty()
                            .toMutableMap()
                            .apply {
                                this["id"] = documento.id
                            }
                    }

                onFinalizado(lista)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudieron cargar los registros " +
                            "pendientes: ${error.message}"
                )
            }
    }

    /**
     * Conserva compatibilidad con MainActivity.
     *
     * APROBADO:
     * finaliza el proceso.
     *
     * DEVUELTO:
     * regresa el documento al usuario para corrección.
     */
    fun actualizarEstadoRevision(
        idRegistro: String,
        nuevoEstado: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        if (idRegistro.isBlank()) {
            onError(
                "No se encontró el identificador del registro."
            )
            return
        }

        val estadoNormalizado =
            nuevoEstado.trim().uppercase()

        if (
            estadoNormalizado != ESTADO_APROBADO &&
            estadoNormalizado != ESTADO_DEVUELTO
        ) {
            onError(
                "El estado de revisión no es válido."
            )
            return
        }

        val cambios = hashMapOf<String, Any>(
            "estadoRegistro" to estadoNormalizado,
            "fechaRevision" to
                    FieldValue.serverTimestamp(),
            "fechaActualizacion" to
                    FieldValue.serverTimestamp(),
            "uidRevisor" to usuarioActual.uid,
            "emailRevisor" to
                    usuarioActual.email.orEmpty()
        )

        if (estadoNormalizado == ESTADO_DEVUELTO) {
            cambios["motivoDevolucion"] =
                "Registro devuelto para corrección."

            cambios["fechaDevolucion"] =
                FieldValue.serverTimestamp()
        }

        if (estadoNormalizado == ESTADO_APROBADO) {
            cambios["fechaAprobacion"] =
                FieldValue.serverTimestamp()

            cambios["motivoDevolucion"] =
                FieldValue.delete()
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .document(idRegistro)
            .update(cambios)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo actualizar el registro: " +
                            "${error.message}"
                )
            }
    }

    /**
     * Versión preparada para devolver indicando
     * un motivo escrito por el revisor.
     *
     * La conectaremos posteriormente a un cuadro
     * de diálogo en la pantalla de revisión.
     */
    fun devolverRegistro(
        idRegistro: String,
        motivoDevolucion: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        if (idRegistro.isBlank()) {
            onError(
                "No se encontró el identificador del registro."
            )
            return
        }

        val motivoNormalizado =
            motivoDevolucion.trim()

        if (motivoNormalizado.isBlank()) {
            onError(
                "Debes indicar el motivo de la devolución."
            )
            return
        }

        val cambios = mapOf<String, Any>(
            "estadoRegistro" to ESTADO_DEVUELTO,
            "motivoDevolucion" to motivoNormalizado,
            "fechaDevolucion" to
                    FieldValue.serverTimestamp(),
            "fechaRevision" to
                    FieldValue.serverTimestamp(),
            "fechaActualizacion" to
                    FieldValue.serverTimestamp(),
            "uidRevisor" to usuarioActual.uid,
            "emailRevisor" to
                    usuarioActual.email.orEmpty()
        )

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .document(idRegistro)
            .update(cambios)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo devolver el registro: " +
                            "${error.message}"
                )
            }
    }

    /**
     * Permite editar tanto BORRADOR como DEVUELTO.
     * Firestore conserva el mismo documento.
     */
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
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        if (idRegistro.isBlank()) {
            onError(
                "No se encontró el identificador del registro."
            )
            return
        }

        val cambios = mapOf<String, Any?>(
            "tipoServicio" to
                    tipoServicio.trim().uppercase(),
            "kilometraje" to
                    convertirNumero(kilometraje),
            "horometro" to
                    convertirNumero(horometro),
            "accionEjecutada" to
                    accionEjecutada.trim(),
            "observaciones" to
                    observaciones.trim(),
            "ordenTrabajo" to
                    ordenTrabajo.trim(),
            "numeroPedido" to
                    numeroPedido.trim(),
            "fechaActualizacion" to
                    FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .document(idRegistro)
            .update(cambios)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo actualizar el registro: " +
                            "${error.message}"
                )
            }
    }

    /**
     * Envía un BORRADOR o reenvía un DEVUELTO.
     * No crea otro documento.
     */
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
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        if (idRegistro.isBlank()) {
            onError(
                "No se encontró el identificador del registro."
            )
            return
        }

        if (accionEjecutada.isBlank()) {
            onError("Debes ingresar la acción ejecutada.")
            return
        }

        val cambios = mapOf<String, Any>(
            "tipoServicio" to
                    tipoServicio.trim().uppercase(),
            "kilometraje" to
                    (convertirNumero(kilometraje) ?: 0.0),
            "horometro" to
                    (convertirNumero(horometro) ?: 0.0),
            "accionEjecutada" to
                    accionEjecutada.trim(),
            "observaciones" to
                    observaciones.trim(),
            "ordenTrabajo" to
                    ordenTrabajo.trim(),
            "numeroPedido" to
                    numeroPedido.trim(),
            "estadoRegistro" to ESTADO_ENVIADO,
            "fechaActualizacion" to
                    FieldValue.serverTimestamp(),
            "fechaEnvio" to
                    FieldValue.serverTimestamp(),
            "motivoDevolucion" to
                    FieldValue.delete()
        )

        FirebaseFirestore.getInstance()
            .collection(COLECCION_REGISTROS)
            .document(idRegistro)
            .update(cambios)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo enviar el registro: " +
                            "${error.message}"
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