package com.luis.mevmantenimiento.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class UsuarioAdministrado(
    val uid: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val cargo: String,
    val rol: String,
    val estadoUsuario: String
)

data class DatosUsuario(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val cargo: String,
    val rol: String,
    val estadoUsuario: String,
    val passwordTemporal: String = ""
)

object UsuariosValidation {
    val roles = listOf(
        "TECNICO_MECANICO",
        "VULCANIZADOR",
        "SUPERVISOR_MANTENIMIENTO",
        "ANALISTA_MANTENIMIENTO",
        "ASISTENTE_PLANIFICACION",
        "PLANIFICADOR",
        "JEFE_OPERACIONES",
        "GERENTE_GENERAL"
    )

    fun validar(datos: DatosUsuario, requierePassword: Boolean): String? {
        if (datos.nombres.isBlank() || datos.apellidos.isBlank() || datos.cargo.isBlank()) {
            return "Completa nombres, apellidos y cargo."
        }
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(datos.email.trim())) {
            return "El correo electrónico no tiene un formato válido."
        }
        if (datos.rol.uppercase() !in roles) {
            return "El rol seleccionado no es válido."
        }
        if (datos.estadoUsuario.uppercase() !in listOf("ACTIVO", "INACTIVO")) {
            return "El estado seleccionado no es válido."
        }
        if (requierePassword && datos.passwordTemporal.length < 6) {
            return "La contraseña temporal debe tener al menos 6 caracteres."
        }
        return null
    }
}

object UsuariosRepository {
    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun cargarUsuarios(
        onFinalizado: (List<UsuarioAdministrado>) -> Unit,
        onError: (String) -> Unit
    ) {
        ejecutarComoPlanificador(
            onAutorizado = {
                firestore.collection("usuarios")
                    .get()
                    .addOnSuccessListener { resultado ->
                        val usuarios = resultado.documents.map { documento ->
                            UsuarioAdministrado(
                                uid = documento.id,
                                nombres = documento.getString("nombres").orEmpty(),
                                apellidos = documento.getString("apellidos").orEmpty(),
                                email = documento.getString("email").orEmpty(),
                                cargo = documento.getString("cargo").orEmpty(),
                                rol = documento.getString("rol").orEmpty().uppercase(),
                                estadoUsuario = documento.getString("estadoUsuario")
                                    .orEmpty()
                                    .ifBlank { "ACTIVO" }
                                    .uppercase()
                            )
                        }.sortedWith(
                            compareBy<UsuarioAdministrado> { it.apellidos.lowercase() }
                                .thenBy { it.nombres.lowercase() }
                        )
                        onFinalizado(usuarios)
                    }
                    .addOnFailureListener { error ->
                        onError(mensajeFirebase(error, "No se pudieron consultar los usuarios."))
                    }
            },
            onError = onError
        )
    }

    fun crearUsuario(
        context: Context,
        datosOriginales: DatosUsuario,
        onFinalizado: (UsuarioAdministrado) -> Unit,
        onError: (String) -> Unit
    ) {
        val datos = normalizar(datosOriginales)
        UsuariosValidation.validar(datos, requierePassword = true)?.let {
            onError(it)
            return
        }

        ejecutarComoPlanificador(
            onAutorizado = {
                val appPrincipal = FirebaseApp.getInstance()
                val nombreApp = "administracion-usuarios-${System.currentTimeMillis()}"
                val appSecundaria = try {
                    FirebaseApp.initializeApp(
                        context.applicationContext,
                        appPrincipal.options,
                        nombreApp
                    )
                } catch (error: Exception) {
                    onError(mensajeFirebase(error, "No se pudo preparar la creación del usuario."))
                    return@ejecutarComoPlanificador
                }
                val authSecundaria = FirebaseAuth.getInstance(appSecundaria)

                fun cerrarSesionSecundaria() {
                    authSecundaria.signOut()
                    appSecundaria.delete()
                }

                authSecundaria.createUserWithEmailAndPassword(
                    datos.email,
                    datos.passwordTemporal
                ).addOnSuccessListener { resultado ->
                    val usuarioFirebase = resultado.user
                    if (usuarioFirebase == null) {
                        cerrarSesionSecundaria()
                        onError("Firebase no devolvió el usuario creado.")
                        return@addOnSuccessListener
                    }

                    val perfilAuth = UserProfileChangeRequest.Builder()
                        .setDisplayName("${datos.nombres} ${datos.apellidos}".trim())
                        .build()

                    usuarioFirebase.updateProfile(perfilAuth)
                        .addOnSuccessListener {
                            val perfil = hashMapOf<String, Any>(
                                "nombres" to datos.nombres,
                                "apellidos" to datos.apellidos,
                                "email" to datos.email,
                                "cargo" to datos.cargo,
                                "rol" to datos.rol,
                                "estadoUsuario" to datos.estadoUsuario,
                                "fechaCreacion" to FieldValue.serverTimestamp(),
                                "fechaActualizacion" to FieldValue.serverTimestamp()
                            )

                            firestore.collection("usuarios")
                                .document(usuarioFirebase.uid)
                                .set(perfil)
                                .addOnSuccessListener {
                                    cerrarSesionSecundaria()
                                    onFinalizado(
                                        UsuarioAdministrado(
                                            uid = usuarioFirebase.uid,
                                            nombres = datos.nombres,
                                            apellidos = datos.apellidos,
                                            email = datos.email,
                                            cargo = datos.cargo,
                                            rol = datos.rol,
                                            estadoUsuario = datos.estadoUsuario
                                        )
                                    )
                                }
                                .addOnFailureListener { error ->
                                    usuarioFirebase.delete().addOnCompleteListener {
                                        cerrarSesionSecundaria()
                                        onError(
                                            mensajeFirebase(
                                                error,
                                                "Se creó la credencial, pero no el perfil. Se revirtió la operación."
                                            )
                                        )
                                    }
                                }
                        }
                        .addOnFailureListener { error ->
                            usuarioFirebase.delete().addOnCompleteListener {
                                cerrarSesionSecundaria()
                                onError(mensajeFirebase(error, "No se pudo completar el perfil del usuario."))
                            }
                        }
                }.addOnFailureListener { error ->
                    cerrarSesionSecundaria()
                    onError(mensajeFirebase(error, "No se pudo crear el usuario."))
                }
            },
            onError = onError
        )
    }

    fun actualizarUsuario(
        uid: String,
        datosOriginales: DatosUsuario,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val datos = normalizar(datosOriginales)
        UsuariosValidation.validar(datos, requierePassword = false)?.let {
            onError(it)
            return
        }
        val uidActual = auth.currentUser?.uid
        if (uid == uidActual && datos.estadoUsuario == "INACTIVO") {
            onError("No puedes inactivar tu propia cuenta.")
            return
        }
        if (uid == uidActual && datos.rol != "PLANIFICADOR") {
            onError("No puedes quitar el rol PLANIFICADOR de tu propia cuenta.")
            return
        }

        ejecutarComoPlanificador(
            onAutorizado = {
                val cambios = mapOf(
                    "nombres" to datos.nombres,
                    "apellidos" to datos.apellidos,
                    "cargo" to datos.cargo,
                    "rol" to datos.rol,
                    "estadoUsuario" to datos.estadoUsuario,
                    "fechaActualizacion" to FieldValue.serverTimestamp()
                )
                firestore.collection("usuarios")
                    .document(uid)
                    .update(cambios)
                    .addOnSuccessListener { onFinalizado() }
                    .addOnFailureListener { error ->
                        onError(mensajeFirebase(error, "No se pudo actualizar el usuario."))
                    }
            },
            onError = onError
        )
    }

    fun enviarRecuperacionPassword(
        email: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onError("El usuario no tiene un correo registrado.")
            return
        }
        ejecutarComoPlanificador(
            onAutorizado = {
                auth.sendPasswordResetEmail(email.trim().lowercase())
                    .addOnSuccessListener { onFinalizado() }
                    .addOnFailureListener { error ->
                        onError(mensajeFirebase(error, "No se pudo enviar el correo de recuperación."))
                    }
            },
            onError = onError
        )
    }

    private fun ejecutarComoPlanificador(
        onAutorizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioActual = auth.currentUser
        if (usuarioActual == null) {
            onError("No existe una sesión de usuario activa.")
            return
        }
        firestore.collection("usuarios")
            .document(usuarioActual.uid)
            .get()
            .addOnSuccessListener { documento ->
                val rol = documento.getString("rol").orEmpty().uppercase()
                val estado = documento.getString("estadoUsuario").orEmpty().uppercase()
                if (rol == "PLANIFICADOR" && estado == "ACTIVO") {
                    onAutorizado()
                } else {
                    onError("Solo un PLANIFICADOR activo puede administrar usuarios.")
                }
            }
            .addOnFailureListener { error ->
                onError(mensajeFirebase(error, "No se pudo verificar el permiso del usuario."))
            }
    }

    private fun normalizar(datos: DatosUsuario): DatosUsuario = datos.copy(
        nombres = datos.nombres.trim(),
        apellidos = datos.apellidos.trim(),
        email = datos.email.trim().lowercase(),
        cargo = datos.cargo.trim(),
        rol = datos.rol.trim().uppercase(),
        estadoUsuario = datos.estadoUsuario.trim().uppercase()
    )

    private fun mensajeFirebase(error: Exception, respaldo: String): String {
        val detalle = error.localizedMessage?.trim().orEmpty()
        return if (detalle.isBlank()) respaldo else "$respaldo $detalle"
    }
}
