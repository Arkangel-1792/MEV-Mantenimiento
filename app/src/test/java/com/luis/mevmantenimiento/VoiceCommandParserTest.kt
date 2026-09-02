package com.luis.mevmantenimiento

import com.luis.mevmantenimiento.voice.VoiceCommand
import com.luis.mevmantenimiento.voice.VoiceCommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun interpretaVariosCamposDeMantenimiento() {
        val comandos = VoiceCommandParser.interpretarVarios(
            "activo volqueta 96 kilometraje 71268 horómetro 2965 " +
                    "tipo de servicio preventivo acción ejecutada cambio de aceite " +
                    "observaciones sin novedad orden de trabajo OT 123 pedido 456"
        )

        assertTrue(
            comandos.contains(
                VoiceCommand.SeleccionarActivo("VVOLQ0096")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarKilometraje("71268")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarHorometro("2965")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarTipoServicio("PREVENTIVO")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarOrdenTrabajo("OT123")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarNumeroPedido("456")
            )
        )
    }

    @Test
    fun reconoceAliasDeHorometro() {
        val comando = VoiceCommandParser.interpretar(
            "orómetro mil doscientos cincuenta"
        )

        assertEquals(
            VoiceCommand.ActualizarHorometro("1250"),
            comando
        )
    }

    @Test
    fun interpretaPosicionYHuella() {
        val comando = VoiceCommandParser.interpretar(
            "posición 10 valor 7 punto 5 milímetros"
        )

        assertEquals(
            VoiceCommand.ActualizarPosicion(
                posicion = 10,
                valor = "7.5"
            ),
            comando
        )
    }

    @Test
    fun interpretaCodigoDeCamioneta() {
        val comando = VoiceCommandParser.interpretar(
            "seleccionar activo camioneta 17"
        )

        assertEquals(
            VoiceCommand.SeleccionarActivo("VCAM0017"),
            comando
        )
    }

    @Test
    fun interpretaIntervencionCompleta() {
        val comandos = VoiceCommandParser.interpretarVarios(
            "activo volqueta 96 tipo de intervención cambio posición 12 " +
                    "huella 5 punto 5 marca pirelli serie abc 123 " +
                    "motivo desgaste técnico Luis Barragán"
        )

        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarTipoIntervencion("CAMBIO")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarPosicionLlanta(12)
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarHuellaLlanta("5.5")
            )
        )
        assertTrue(
            comandos.contains(
                VoiceCommand.ActualizarTecnico("Luis Barragan")
            )
        )
    }

    @Test
    fun reconoceAccionesDeGuardado() {
        assertEquals(
            VoiceCommand.GuardarBorrador,
            VoiceCommandParser.interpretar("guardar borrador")
        )
        assertEquals(
            VoiceCommand.EnviarRegistro,
            VoiceCommandParser.interpretar("enviar registro")
        )
    }
}
