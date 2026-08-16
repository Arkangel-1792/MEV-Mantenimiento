package com.luis.mevmantenimiento.ui.screens

data class ReporteResumen(
    val totalMantenimientos: Int = 0,
    val preventivos: Int = 0,
    val correctivos: Int = 0,
    val mantenimientosEnviados: Int = 0,
    val mantenimientosAprobados: Int = 0,
    val mantenimientosDevueltos: Int = 0,

    val totalHuellas: Int = 0,
    val huellasCriticas: Int = 0,

    val totalIntervenciones: Int = 0,
    val cambios: Int = 0,
    val rotaciones: Int = 0,
    val reparaciones: Int = 0,
    val montajes: Int = 0,
    val desmontajes: Int = 0,
    val bajas: Int = 0,

    val registrosPendientesRevision: Int = 0
)