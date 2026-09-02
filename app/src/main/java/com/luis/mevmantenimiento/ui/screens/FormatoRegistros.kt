package com.luis.mevmantenimiento.ui.screens

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun formatearFechaRegistro(
    datos: Map<String, Any?>
): String {
    val timestamp =
        datos["fechaEnvio"] as? Timestamp
            ?: datos["fechaActualizacion"] as? Timestamp
            ?: datos["fechaCreacion"] as? Timestamp
            ?: return ""

    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(timestamp.toDate())
}
