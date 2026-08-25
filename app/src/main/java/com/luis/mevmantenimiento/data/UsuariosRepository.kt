package com.luis.mevmantenimiento.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class UsuarioResumen(
    val uid: String = "",
    val nombres: String,
    val apellidos: String,
    val email: String,
    val cargo: String,
    val rol: String,
    val estadoUsuario: String = "ACTIVO"
)

object UsuariosRepository {

    val rolesDisponibles = listOf(
        "TECNICO_MECANICO",
        "VULCANIZADOR",
        "SUPERVISOR_MANTENIMIENTO",
        "ANALISTA_MANTENIMIENTO",
        "ASISTENTE_PLANIFICACION",
        "PLANIFICADOR",
        "JEFE_OPERACIONES",
        "GERENTE_GENERAL"
    )

    private const val COLECCION = "usuarios"
    private const val APP_SECUNDARIA = "AdministracionUsuarios"

    fun cargar(
        onFinalizado: (List<UsuarioResumen>) -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection(COLECCION)
            .get()
            .addOnSuccessListener { resultado ->
                val usuarios = resultado.documents.map { documento ->
                    UsuarioResumen(
                        uid = documento.id,
                        nombres = documento.getString("nombres").orEmpty(),
                        apellidos = documento.getString("apellidos").orEmpty(),
                        email = documento.getString("email").orEmpty(),
                        cargo = documento.getString("cargo").orEmpty(),
                        rol = documento.getString("rol").orEmpty(),
                        estadoUsuario =
                            documento.getString("estadoUsuario")
                                .orEmpty()
                                .ifBlank { "ACTIVO" }
                    )
                }.sortedWith(
                    compareBy<UsuarioResumen> { it.apellidos.lowercase() }
                        .thenBy { it.nombres.lowercase() }
                )

                onFinalizado(usuarios)
            }
            .addOnFailureListener { error ->
                onError("No se pudieron cargar los usuarios: ${error.message}")
            }
    }

    fun crear(
        context: Context,
        usuario: UsuarioResumen,
        passwordTemporal: String,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val errorValidacion = validar(usuario, passwordTemporal, esNuevo = true)
        if (errorValidacion != null) {
            onError(errorValidacion)
            return
        }

        val appSecundaria = obtenerAppSecundaria(context)
        val authSecundaria = FirebaseAuth.getInstance(appSecundaria)

        authSecundaria.createUserWithEmailAndPassword(
            usuario.email.trim().lowercase(),
            passwordTemporal
        ).addOnSuccessListener { resultado ->
            val uid = resultado.user?.uid

            if (uid == null) {
                authSecundaria.signOut()
                onError("Firebase no devolvió el identificador del usuario.")
                return@addOnSuccessListener
            }

            guardarPerfil(
                usuario = usuario.copy(uid = uid),
                esNuevo = true,
                onFinalizado = {
                    authSecundaria.signOut()
                    onFinalizado()
                },
                onError = {
                    authSecundaria.signOut()
                    onError(it)
                }
            )
        }.addOnFailureListener { error ->
            authSecundaria.signOut()
            onError("No se pudo crear la cuenta: ${error.message}")
        }
    }

    fun actualizar(
        usuario: UsuarioResumen,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (
            usuario.uid == FirebaseAuth.getInstance().currentUser?.uid &&
            usuario.estadoUsuario.uppercase() == "INACTIVO"
        ) {
            onError("No puedes desactivar tu propio usuario.")
            return
        }

        val errorValidacion = validar(usuario, "", esNuevo = false)
        if (errorValidacion != null) {
            onError(errorValidacion)
            return
        }

        guardarPerfil(
            usuario = usuario,
            esNuevo = false,
            onFinalizado = onFinalizado,
            onError = onError
        )
    }

    private fun guardarPerfil(
        usuario: UsuarioResumen,
        esNuevo: Boolean,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        val datos = mutableMapOf<String, Any>(
            "nombres" to usuario.nombres.trim(),
            "apellidos" to usuario.apellidos.trim(),
            "email" to usuario.email.trim().lowercase(),
            "cargo" to usuario.cargo.trim(),
            "rol" to usuario.rol.trim().uppercase(),
            "estadoUsuario" to usuario.estadoUsuario.trim().uppercase(),
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        if (esNuevo) {
            datos["fechaCreacion"] = FieldValue.serverTimestamp()
        }

        FirebaseFirestore.getInstance()
            .collection(COLECCION)
            .document(usuario.uid)
            .set(datos, SetOptions.merge())
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError("No se pudo guardar el perfil: ${error.message}")
            }
    }

    private fun validar(
        usuario: UsuarioResumen,
        passwordTemporal: String,
        esNuevo: Boolean
    ): String? {
        if (
            usuario.nombres.isBlank() ||
            usuario.email.isBlank() ||
            usuario.rol.isBlank()
        ) {
            return "Nombres, correo y rol son obligatorios."
        }

        if (usuario.rol.uppercase() !in rolesDisponibles) {
            return "El rol seleccionado no es válido."
        }

        if (usuario.estadoUsuario.uppercase() !in setOf("ACTIVO", "INACTIVO")) {
            return "El estado del usuario no es válido."
        }

        if (esNuevo && passwordTemporal.length < 6) {
            return "La contraseña temporal debe tener al menos 6 caracteres."
        }

        if (!esNuevo && usuario.uid.isBlank()) {
            return "No se encontró el identificador del usuario."
        }

        return null
    }

    private fun obtenerAppSecundaria(
        context: Context
    ): FirebaseApp {
        return FirebaseApp.getApps(context)
            .firstOrNull { it.name == APP_SECUNDARIA }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseApp.getInstance().options,
                APP_SECUNDARIA
            )
    }
}
