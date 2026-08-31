package com.luis.mevmantenimiento.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun reconoceTractorPorTipoYNumero() {
        val comando = VoiceCommandParser.interpretar("código tractor 9")

        assertTrue(comando is VoiceCommand.SeleccionarActivo)
        assertEquals("MTRAC0009", (comando as VoiceCommand.SeleccionarActivo).codigo)
    }

    @Test
    fun reconoceMiniComoMinicargadora() {
        val comando = VoiceCommandParser.interpretar("activo mini 5")

        assertTrue(comando is VoiceCommand.SeleccionarActivo)
        assertEquals("MMINI0005", (comando as VoiceCommand.SeleccionarActivo).codigo)
    }

    @Test
    fun reconoceMantenimientoRealizadoComoAccion() {
        val comando = VoiceCommandParser.interpretar("mantenimiento realizado cambio de bandas")

        assertTrue(comando is VoiceCommand.ActualizarAccionEjecutada)
        assertEquals(
            "cambio de bandas",
            (comando as VoiceCommand.ActualizarAccionEjecutada).valor
        )
    }

    @Test
    fun separaActivoYMantenimientoEnUnDictado() {
        val comandos = VoiceCommandParser.interpretarVarios(
            "tractor 9 mantenimiento realizado cambio de bandas"
        )

        assertEquals(2, comandos.size)
        assertEquals("MTRAC0009", (comandos[0] as VoiceCommand.SeleccionarActivo).codigo)
        assertEquals(
            "cambio de bandas",
            (comandos[1] as VoiceCommand.ActualizarAccionEjecutada).valor
        )
    }
}
