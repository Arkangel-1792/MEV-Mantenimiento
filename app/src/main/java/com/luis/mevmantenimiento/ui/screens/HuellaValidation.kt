package com.luis.mevmantenimiento.ui.screens

internal const val HUELLA_MAXIMA_MM = 26.0

internal fun convertirHuella(valor: String): Double? =
    valor.trim().replace(',', '.').toDoubleOrNull()

internal fun aceptarEntradaHuella(valor: String): Boolean {
    if (valor.isBlank()) return true
    if (!valor.matches(Regex("""\d{0,2}([.,]\d*)?"""))) return false
    return convertirHuella(valor)?.let { it <= HUELLA_MAXIMA_MM } ?: true
}
