package com.luis.mevmantenimiento

import com.luis.mevmantenimiento.ui.screens.ActivoResumen
import com.luis.mevmantenimiento.ui.screens.esHuellaCritica
import com.luis.mevmantenimiento.ui.screens.validarIndicadoresActivo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesFormularioTest {

    @Test
    fun activoPorHorometroExigeSoloHorometro() {
        val activo = activoBase(
            usaKilometraje = false,
            usaHorometro = true
        )

        assertNotNull(
            validarIndicadoresActivo(activo, "", "")
        )
        assertNull(
            validarIndicadoresActivo(activo, "", "1250,5")
        )
    }

    @Test
    fun activoPorKilometrajeExigeSoloKilometraje() {
        val activo = activoBase(
            usaKilometraje = true,
            usaHorometro = false
        )

        assertNotNull(
            validarIndicadoresActivo(activo, "", "")
        )
        assertNull(
            validarIndicadoresActivo(activo, "48000", "")
        )
    }

    @Test
    fun identificaLimiteDeHuellaCritica() {
        assertTrue(esHuellaCritica("6"))
        assertTrue(esHuellaCritica(5.9))
        assertFalse(esHuellaCritica("6.1"))
        assertFalse(esHuellaCritica(""))
    }

    private fun activoBase(
        usaKilometraje: Boolean,
        usaHorometro: Boolean
    ): ActivoResumen {
        return ActivoResumen(
            codigo = "VEQ0001",
            subtipo = "EXCAVADORA",
            tipo = "MAQUINARIA",
            marca = "CAT",
            modelo = "320D",
            indicador = if (usaHorometro) "HR" else "KM",
            horometro = 1000.0,
            kilometraje = 50000.0,
            ubicacionActual = "Villonaco",
            status = "OPERATIVA",
            usaKilometraje = usaKilometraje,
            usaHorometro = usaHorometro
        )
    }
}
