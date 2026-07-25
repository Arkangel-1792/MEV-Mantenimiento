package com.luis.mevmantenimiento.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray

object ImportadorActivos {

    fun importar(
        context: Context,
        onProgreso: (actual: Int, total: Int) -> Unit,
        onFinalizado: (cantidad: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val jsonTexto = context.assets
                .open("inventario_activos_firestore.json")
                .bufferedReader()
                .use { lector ->
                    lector.readText()
                }

            val jsonArray = JSONArray(jsonTexto)
            val firestore = FirebaseFirestore.getInstance()

            var indice = 0
            var importados = 0

            fun guardarSiguiente() {
                if (indice >= jsonArray.length()) {
                    onFinalizado(importados)
                    return
                }

                val jsonActivo = jsonArray.getJSONObject(indice)
                val codigo = jsonActivo.optString("codigo").trim()

                if (codigo.isBlank()) {
                    indice++
                    guardarSiguiente()
                    return
                }

                val datos = mutableMapOf<String, Any?>()
                val claves = jsonActivo.keys()

                while (claves.hasNext()) {
                    val clave = claves.next()
                    val valor = jsonActivo.opt(clave)

                    datos[clave] = when {
                        valor == null -> null
                        valor.toString() == "null" -> null
                        else -> valor
                    }
                }

                firestore.collection("activos")
                    .document(codigo)
                    .set(datos)
                    .addOnSuccessListener {
                        importados++
                        indice++
                        onProgreso(indice, jsonArray.length())
                        guardarSiguiente()
                    }
                    .addOnFailureListener { error ->
                        onError(
                            "No se pudo importar $codigo: ${error.message}"
                        )
                    }
            }

            guardarSiguiente()

        } catch (error: Exception) {
            onError(
                "No se pudo leer el archivo de activos: ${error.message}"
            )
        }
    }

    fun cargarActivos(
        onFinalizado: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("activos")
            .get()
            .addOnSuccessListener { resultado ->

                val lista = resultado.documents.map { documento ->
                    documento.data.orEmpty()
                }

                onFinalizado(lista)
            }
            .addOnFailureListener { error ->
                onError(
                    "No se pudieron cargar los activos: ${error.message}"
                )
            }
    }
}
