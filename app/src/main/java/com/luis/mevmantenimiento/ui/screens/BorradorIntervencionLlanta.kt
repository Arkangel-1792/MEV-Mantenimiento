package com.luis.mevmantenimiento.ui.screens

data class BorradorIntervencionLlanta(
    val id: String,
    val codigoActivo: String,
    val proyecto: String,
    val kilometraje: Double?,
    val horometro: Double?,
    val tipoIntervencion: String,
    val posicion: String,
    val huella: Double?,
    val marcaLlanta: String,
    val medidaLlanta: String,
    val serieLlanta: String,
    val motivo: String,
    val observaciones: String,
    val nombreTecnico: String,
    val estadoRegistro: String,
    val motivoDevolucion: String = ""
)