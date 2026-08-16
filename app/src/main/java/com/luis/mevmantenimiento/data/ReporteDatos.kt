package com.luis.mevmantenimiento.data

import com.luis.mevmantenimiento.ui.screens.ReporteResumen

data class ReporteDatos(
    val resumen: ReporteResumen,
    val mantenimientos: List<Map<String, Any?>>,
    val huellas: List<Map<String, Any?>>,
    val intervenciones: List<Map<String, Any?>>
)