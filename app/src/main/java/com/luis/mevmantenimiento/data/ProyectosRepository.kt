package com.luis.mevmantenimiento.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class ProyectoResumen(
    val id: String = "",
    val nombre: String,
    val ciudad: String,
    val ubicacion: String,
    val descripcion: String,
    val activo: Boolean = true
)

object ProyectosRepository {

    private const val COLECCION = "proyectos"

    fun cargar(
        onFinalizado: (List<ProyectoResumen>) -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection(COLECCION)
            .get()
            .addOnSuccessListener { resultado ->
                val proyectos = resultado.documents.map { documento ->
                    ProyectoResumen(
                        id = documento.id,
                        nombre = documento.getString("nombre").orEmpty(),
                        ciudad = documento.getString("ciudad").orEmpty(),
                        ubicacion = documento.getString("ubicacion").orEmpty(),
                        descripcion =
                            documento.getString("descripcion").orEmpty(),
                        activo = documento.getBoolean("activo") ?: true
                    )
                }.sortedBy { it.nombre.lowercase() }

                onFinalizado(proyectos)
            }
            .addOnFailureListener { error ->
                onError("No se pudieron cargar los proyectos: ${error.message}")
            }
    }

    fun guardar(
        proyecto: ProyectoResumen,
        onFinalizado: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (proyecto.nombre.isBlank()) {
            onError("El nombre del proyecto es obligatorio.")
            return
        }

        val coleccion = FirebaseFirestore.getInstance().collection(COLECCION)
        val documento = if (proyecto.id.isBlank()) {
            coleccion.document()
        } else {
            coleccion.document(proyecto.id)
        }

        val datos = mutableMapOf<String, Any>(
            "nombre" to proyecto.nombre.trim(),
            "ciudad" to proyecto.ciudad.trim(),
            "ubicacion" to proyecto.ubicacion.trim(),
            "descripcion" to proyecto.descripcion.trim(),
            "activo" to proyecto.activo,
            "fechaActualizacion" to FieldValue.serverTimestamp()
        )

        if (proyecto.id.isBlank()) {
            datos["fechaCreacion"] = FieldValue.serverTimestamp()
        }

        documento.set(datos, SetOptions.merge())
            .addOnSuccessListener {
                onFinalizado()
            }
            .addOnFailureListener { error ->
                onError("No se pudo guardar el proyecto: ${error.message}")
            }
    }
}
