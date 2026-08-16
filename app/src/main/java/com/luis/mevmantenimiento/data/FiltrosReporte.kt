package com.luis.mevmantenimiento.data

data class FiltrosReporte(
    val proyecto: String = "",
    val activo: String = "",
    val tecnico: String = "",
    val estado: String = "",
    val fechaDesde: String = "",
    val fechaHasta: String = ""
)