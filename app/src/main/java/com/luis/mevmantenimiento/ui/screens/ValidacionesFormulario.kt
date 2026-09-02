package com.luis.mevmantenimiento.ui.screens

fun validarIndicadoresActivo(
    activo: ActivoResumen?,
    kilometraje: String,
    horometro: String
): String? {
    if (activo == null) {
        return "Debes seleccionar un activo válido."
    }

    if (
        activo.usaKilometraje &&
        kilometraje.numeroFormulario() == null
    ) {
        return "Debes ingresar un kilometraje válido para este activo."
    }

    if (
        activo.usaHorometro &&
        horometro.numeroFormulario() == null
    ) {
        return "Debes ingresar un horómetro válido para este activo."
    }

    return null
}

fun esHuellaCritica(
    valor: String
): Boolean {
    return valor.numeroFormulario()
        ?.let { it <= 6.0 }
        ?: false
}

fun esHuellaCritica(
    valor: Double?
): Boolean {
    return valor != null && valor <= 6.0
}

private fun String.numeroFormulario(): Double? {
    return trim()
        .replace(",", ".")
        .toDoubleOrNull()
}
