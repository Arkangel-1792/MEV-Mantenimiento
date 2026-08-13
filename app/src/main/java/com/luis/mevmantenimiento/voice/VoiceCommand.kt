package com.luis.mevmantenimiento.voice

sealed class VoiceCommand {

    // ===== COMANDOS GENERALES =====

    data class SeleccionarActivo(
        val codigo: String
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

    data class ActualizarTecnico(
        val valor: String
    ) : VoiceCommand()


    // ===== TOMA DE HUELLA =====

    data class ActualizarPosicion(
        val posicion: Int,
        val valor: String
    ) : VoiceCommand()


    // ===== MANTENIMIENTO =====

    data class ActualizarTipoServicio(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarAccionEjecutada(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarOrdenTrabajo(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarNumeroPedido(
        val valor: String
    ) : VoiceCommand()


    // ===== INTERVENCIÓN DE LLANTA =====

    data class ActualizarTipoIntervencion(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarPosicionLlanta(
        val posicion: Int
    ) : VoiceCommand()

    data class ActualizarHuellaLlanta(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarMarcaLlanta(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarMedidaLlanta(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarSerieLlanta(
        val valor: String
    ) : VoiceCommand()

    data class ActualizarMotivoIntervencion(
        val valor: String
    ) : VoiceCommand()


    // ===== ACCIONES =====

    data object GuardarBorrador : VoiceCommand()

    data object EnviarRegistro : VoiceCommand()


    // ===== RESPALDO =====

    data class Desconocido(
        val textoOriginal: String
    ) : VoiceCommand()
}