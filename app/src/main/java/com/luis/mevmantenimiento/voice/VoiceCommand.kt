package com.luis.mevmantenimiento.voice

sealed class VoiceCommand {

    data class SeleccionarActivo(
        val codigo: String
    ) : VoiceCommand()

    data class ActualizarPosicion(
        val posicion: Int,
        val valor: String
    ) : VoiceCommand()

    data class ActualizarProyecto(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarKilometraje(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarHorometro(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarEstadoGeneral(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarNovedad(
        val valor: String
    ) : VoiceCommand()

    data object GuardarBorrador : VoiceCommand()

    data object EnviarRegistro : VoiceCommand()

    data class Desconocido(
        val textoOriginal: String
    ) : VoiceCommand()
}