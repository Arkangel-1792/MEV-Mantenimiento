package com.luis.mevmantenimiento

import com.luis.mevmantenimiento.ui.screens.ActivoResumen
import com.luis.mevmantenimiento.ui.screens.cantidadPosicionesDelActivo
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivoConfiguracionTest {

    @Test
    fun usaLaCantidadConfiguradaEnElActivo() {
        val activo = activoBase(
            cantidadPosiciones = 8
        )

        assertEquals(8, cantidadPosicionesDelActivo(activo))
    }

    @Test
    fun activoSinVulcanizacionNoTienePosiciones() {
        val activo = activoBase(
            aplicaVulcanizacion = false,
            cantidadPosiciones = 12
        )

        assertEquals(0, cantidadPosicionesDelActivo(activo))
    }

    @Test
    fun conservaCompatibilidadConVolquetaShacman() {
        val activo = activoBase(
            cantidadPosiciones = 0
        )

        assertEquals(12, cantidadPosicionesDelActivo(activo))
    }

    private fun activoBase(
        aplicaVulcanizacion: Boolean = true,
        cantidadPosiciones: Int
    ): ActivoResumen {
        return ActivoResumen(
            codigo = "VVOLQ0096",
            subtipo = "VOLQUETA",
            tipo = "VEHICULOS",
            marca = "SHACMAN",
            modelo = "X3000",
            indicador = "HR",
            horometro = 1000.0,
            kilometraje = 50000.0,
            ubicacionActual = "Villonaco",
            status = "OPERATIVA",
            aplicaVulcanizacion = aplicaVulcanizacion,
            cantidadPosiciones = cantidadPosiciones
        )
    }
}
