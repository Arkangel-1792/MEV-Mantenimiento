package com.luis.mevmantenimiento.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object VulcanizacionRepository {

    private const val COLECCION_TOMAS_HUELLA =
        "tomas_huella"

    const val ESTADO_BORRADOR = "BORRADOR"
    const val ESTADO_ENVIADO = "ENVIADO"
    const val ESTADO_APROBADO = "APROBADO"
    const val ESTADO_DEVUELTO = "DEVUELTO"

    fun guardarTomaHuella(
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String,
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

        val errorValidacion = validarDatos(
            codigoActivo = codigoNormalizado,
            huellas = huellas,
            estadoGeneral = estadoGeneral,
            nombreTecnico = nombreTecnico,
            estadoRegistro = estadoNormalizado
        )

        if (errorValidacion != null) {
            onError(errorValidacion)
            return
        }

        val datos = construirDatos(
            codigoActivo = codigoNormalizado,
            proyecto = proyecto,
            kilometraje = kilometraje,
            horometro = horometro,
            huellas = huellas,
            estadoGeneral = estadoGeneral,
            novedad = novedad,
            nombreTecnico = nombreTecnico,
            estadoRegistro = estadoNormalizado,
            uidUsuario = usuarioActual.uid,
            emailUsuario = usuarioActual.email.orEmpty()
        )

        datos["fechaCreacion"] =
            FieldValue.serverTimestamp()

        if (estadoNormalizado == ESTADO_ENVIADO) {
            datos["fechaEnvio"] =
                FieldValue.serverTimestamp()
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .add(datos)
            .addOnSuccessListener { documento ->
                onFinalizado(documento.id)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo guardar la toma de huella: " +
                            error.message.orEmpty()
                )
            }
    }

    fun cargarMisBorradores(
        onFinalizado: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid =
            FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .whereEqualTo("uidUsuario", uid)
            .whereEqualTo(
                "estadoRegistro",
                ESTADO_BORRADOR
            )
            .get()
            .addOnSuccessListener { resultado ->

                val registros = resultado.documents.map { documento ->
                    documento.data.orEmpty().toMutableMap().apply {
                        this["id"] = documento.id
                    }
                }

                onFinalizado(registros)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudieron cargar los borradores: " +
                            error.message.orEmpty()
                )
            }
    }

    fun cargarMiHistorial(
        onFinalizado: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid =
            FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .whereEqualTo("uidUsuario", uid)
            .get()
            .addOnSuccessListener { resultado ->

                val registros = resultado.documents
                    .map { documento ->
                        documento.data.orEmpty().toMutableMap().apply {
                            this["id"] = documento.id
                        }
                    }
                    .filter { registro ->
                        registro["estadoRegistro"]?.toString() in listOf(
                            ESTADO_ENVIADO,
                            ESTADO_APROBADO,
                            ESTADO_DEVUELTO
                        )
                    }

                onFinalizado(registros)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo cargar el historial: " +
                            error.message.orEmpty()
                )
            }
    }

    fun actualizarBorrador(
        idRegistro: String,
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        actualizarRegistro(
            idRegistro = idRegistro,
            codigoActivo = codigoActivo,
            proyecto = proyecto,
            kilometraje = kilometraje,
            horometro = horometro,
            huellas = huellas,
            estadoGeneral = estadoGeneral,
            novedad = novedad,
            nombreTecnico = nombreTecnico,
            nuevoEstado = ESTADO_BORRADOR,
            onFinalizado = onFinalizado,
            onError = onError
        )
    }

    fun enviarBorrador(
        idRegistro: String,
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        actualizarRegistro(
            idRegistro = idRegistro,
            codigoActivo = codigoActivo,
            proyecto = proyecto,
            kilometraje = kilometraje,
            horometro = horometro,
            huellas = huellas,
            estadoGeneral = estadoGeneral,
            novedad = novedad,
            nombreTecnico = nombreTecnico,
            nuevoEstado = ESTADO_ENVIADO,
            onFinalizado = onFinalizado,
            onError = onError
        )
    }

    private fun actualizarRegistro(
        idRegistro: String,
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String,
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
            onError("No se pudo identificar el registro.")
            return
        }

        val codigoNormalizado =
            codigoActivo.trim().uppercase()

        val errorValidacion = validarDatos(
            codigoActivo = codigoNormalizado,
            huellas = huellas,
            estadoGeneral = estadoGeneral,
            nombreTecnico = nombreTecnico,
            estadoRegistro = nuevoEstado
        )

        if (errorValidacion != null) {
            onError(errorValidacion)
            return
        }

        val datos = construirDatos(
            codigoActivo = codigoNormalizado,
            proyecto = proyecto,
            kilometraje = kilometraje,
            horometro = horometro,
            huellas = huellas,
            estadoGeneral = estadoGeneral,
            novedad = novedad,
            nombreTecnico = nombreTecnico,
            estadoRegistro = nuevoEstado,
            uidUsuario = usuarioActual.uid,
            emailUsuario = usuarioActual.email.orEmpty()
        )

        if (nuevoEstado == ESTADO_ENVIADO) {
            datos["fechaEnvio"] =
                FieldValue.serverTimestamp()
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .document(idRegistro)
            .update(datos)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo actualizar la toma de huella: " +
                            error.message.orEmpty()
                )
            }
    }

    fun cargarRegistrosPendientesRevision(
        onFinalizado: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual =
            FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .whereEqualTo(
                "estadoRegistro",
                ESTADO_ENVIADO
            )
            .get()
            .addOnSuccessListener { resultado ->

                val registros = resultado.documents
                    .map { documento ->
                        documento.data
                            .orEmpty()
                            .toMutableMap()
                            .apply {
                                this["id"] = documento.id
                            }
                    }

                onFinalizado(registros)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudieron cargar las tomas pendientes: " +
                            error.message.orEmpty()
                )
            }
    }

    fun aprobarTomaHuella(
        idRegistro: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (idRegistro.isBlank()) {
            onError("No se pudo identificar la toma de huella.")
            return
        }

        val datosActualizacion = hashMapOf<String, Any?>(
            "estadoRegistro" to ESTADO_APROBADO,
            "motivoDevolucion" to "",
            "fechaAprobacion" to FieldValue.serverTimestamp(),
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .document(idRegistro)
            .update(datosActualizacion)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo aprobar la toma de huella: " +
                            error.message.orEmpty()
                )
            }
    }
    fun devolverTomaHuella(
        idRegistro: String,
        motivoDevolucion: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (idRegistro.isBlank()) {
            onError("No se pudo identificar la toma de huella.")
            return
        }

        val motivoNormalizado =
            motivoDevolucion.trim()

        if (motivoNormalizado.isBlank()) {
            onError("Debes ingresar el motivo de devolución.")
            return
        }

        val datosActualizacion = hashMapOf<String, Any?>(
            "estadoRegistro" to ESTADO_DEVUELTO,
            "motivoDevolucion" to motivoNormalizado,
            "fechaDevolucion" to FieldValue.serverTimestamp(),
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection(COLECCION_TOMAS_HUELLA)
            .document(idRegistro)
            .update(datosActualizacion)
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudo devolver la toma de huella: " +
                            error.message.orEmpty()
                )
            }
    }
    private fun construirDatos(
        codigoActivo: String,
        proyecto: String,
        kilometraje: String,
        horometro: String,
        huellas: List<String>,
        estadoGeneral: String,
        novedad: String,
        nombreTecnico: String,
        estadoRegistro: String,
        uidUsuario: String,
        emailUsuario: String
    ): HashMap<String, Any?> {

        val datos = hashMapOf<String, Any?>(
            "codigoActivo" to codigoActivo,
            "proyecto" to proyecto.trim(),
            "kilometraje" to convertirNumero(kilometraje),
            "horometro" to convertirNumero(horometro),
            "estadoGeneral" to estadoGeneral.trim(),
            "novedad" to novedad.trim(),
            "nombreTecnico" to nombreTecnico.trim(),
            "estadoRegistro" to estadoRegistro,
            "uidUsuario" to uidUsuario,
            "emailUsuario" to emailUsuario,
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        for (indice in 0 until 12) {
            val valor =
                huellas.getOrNull(indice).orEmpty()

            datos["P${indice + 1}"] =
                convertirNumero(valor)
        }

        return datos
    }

    private fun validarDatos(
        codigoActivo: String,
        huellas: List<String>,
        estadoGeneral: String,
        nombreTecnico: String,
        estadoRegistro: String
    ): String? {

        if (codigoActivo.isBlank()) {
            return "Debes seleccionar un activo."
        }

        if (huellas.size != 12) {
            return "La toma debe contener las posiciones P1 a P12."
        }

        if (
            estadoRegistro != ESTADO_BORRADOR &&
            estadoRegistro != ESTADO_ENVIADO
        ) {
            return "El estado del registro no es válido."
        }

        if (estadoRegistro == ESTADO_ENVIADO) {

            if (nombreTecnico.isBlank()) {
                return "Debes ingresar el nombre del técnico."
            }

            if (estadoGeneral.isBlank()) {
                return "Debes ingresar el estado general de las llantas."
            }

            if (huellas.none { it.isNotBlank() }) {
                return "Debes registrar al menos una medida de huella."
            }
        }

        return null
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