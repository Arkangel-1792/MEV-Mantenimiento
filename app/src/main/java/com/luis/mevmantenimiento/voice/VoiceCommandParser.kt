package com.luis.mevmantenimiento.voice

import java.text.Normalizer

object VoiceCommandParser {

    fun interpretar(
        textoOriginal: String
    ): VoiceCommand {

        val texto =
            normalizarTexto(textoOriginal)

        if (
            texto.contains("guardar borrador") ||
            texto == "guardar"
        ) {
            return VoiceCommand.GuardarBorrador
        }

        if (
            texto.contains("enviar registro") ||
            texto == "enviar"
        ) {
            return VoiceCommand.EnviarRegistro
        }

        interpretarActivo(texto)?.let {
            return it
        }

        interpretarPosicion(texto)?.let {
            return it
        }

        extraerValorDespuesDe(
            texto,
            listOf("proyecto")
        )?.let { valor ->

            return VoiceCommand.ActualizarProyecto(
                valor = normalizarProyecto(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "kilometraje",
                "kilometros",
                "kilometro"
            )
        )?.let { valor ->

            return VoiceCommand.ActualizarKilometraje(
                valor = convertirKilometraje(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "horometro",
                "orometro",
                "odometro",
                "contador de horas",
                "horas del equipo",
                "horas"
            )
        )?.let { valor ->

            return VoiceCommand.ActualizarHorometro(
                valor = convertirTextoNumerico(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "estado general",
                "estado"
            )
        )?.let { valor ->

            return VoiceCommand.ActualizarEstadoGeneral(
                valor = valor
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "novedad",
                "observacion"
            )
        )?.let { valor ->

            return VoiceCommand.ActualizarNovedad(
                valor = valor
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "nombre del tecnico",
                "nombre tecnico",
                "tecnico",
                "vulcanizador",
                "mecanico"
            )
        )?.let { valor ->

            return VoiceCommand.ActualizarTecnico(
                valor = normalizarNombre(valor)
            )
        }

        return VoiceCommand.Desconocido(
            textoOriginal = textoOriginal
        )
    }


    fun interpretarVarios(
        textoOriginal: String
    ): List<VoiceCommand> {

        val texto = normalizarTexto(textoOriginal)

        if (texto.isBlank()) {
            return listOf(
                VoiceCommand.Desconocido(textoOriginal)
            )
        }

        /*
         * Detecta dónde comienza cada dato/comando.
         * Esto permite decir de corrido, por ejemplo:
         *
         * "Volqueta 99 proyecto Villonaco kilometraje 125 mil
         * horometro 8500 posicion 1 8 milimetros
         * posicion 2 7 punto 5 milimetros tecnico Luis Barragan"
         */
        val patronInicio = Regex(
            """\b(?:seleccionar\s+activo|activo|volqueta|camioneta|equipo|proyecto|kilometraje|kilometros?|horometro|orometro|odometro|contador\s+de\s+horas|horas\s+del\s+equipo|estado\s+general|novedad|observacion|nombre\s+del\s+tecnico|nombre\s+tecnico|tecnico|vulcanizador|mecanico|posicion\s+\d{1,2}|p\s*\d{1,2}|guardar\s+borrador|enviar\s+registro)\b"""
        )

        val inicios = patronInicio
            .findAll(texto)
            .map { it.range.first }
            .distinct()
            .toList()

        if (inicios.isEmpty()) {
            return listOf(interpretar(textoOriginal))
        }

        val segmentos = mutableListOf<String>()

        inicios.forEachIndexed { indice, inicio ->
            val fin = if (indice < inicios.lastIndex) {
                inicios[indice + 1]
            } else {
                texto.length
            }

            val segmento = texto
                .substring(inicio, fin)
                .trim()
                .trim(',', ';', '.', ' ')

            if (segmento.isNotBlank()) {
                segmentos.add(segmento)
            }
        }

        val comandos = segmentos
            .map { segmento ->
                interpretar(segmento)
            }
            .filterNot { comando ->
                comando is VoiceCommand.Desconocido
            }

        return if (comandos.isNotEmpty()) {
            comandos
        } else {
            listOf(
                VoiceCommand.Desconocido(textoOriginal)
            )
        }
    }

    private fun interpretarActivo(
        texto: String
    ): VoiceCommand.SeleccionarActivo? {

        if (
            !texto.contains("activo") &&
            !texto.contains("volqueta") &&
            !texto.contains("camioneta") &&
            !texto.contains("equipo")
        ) {
            return null
        }

        val codigoDirecto =
            Regex(
                """vvolq\s*0*(\d+)"""
            ).find(texto)

        if (codigoDirecto != null) {

            val numero =
                codigoDirecto
                    .groupValues[1]
                    .toIntOrNull()
                    ?: return null

            return VoiceCommand.SeleccionarActivo(
                codigo =
                    construirCodigoVolqueta(numero)
            )
        }

        val numero =
            extraerPrimerNumero(texto)
                ?: convertirPalabraNumero(
                    texto.substringAfterLast(" ")
                )
                ?: return null

        return when {

            texto.contains("volqueta") -> {
                VoiceCommand.SeleccionarActivo(
                    codigo =
                        construirCodigoVolqueta(numero)
                )
            }

            texto.contains("camioneta") -> {
                VoiceCommand.SeleccionarActivo(
                    codigo =
                        construirCodigoCamioneta(numero)
                )
            }

            else -> {
                VoiceCommand.SeleccionarActivo(
                    codigo = numero.toString()
                )
            }
        }
    }

    private fun interpretarPosicion(
        texto: String
    ): VoiceCommand.ActualizarPosicion? {

        if (
            !texto.contains("posicion") &&
            !Regex("""\bp\s*\d+""")
                .containsMatchIn(texto)
        ) {
            return null
        }

        val coincidencia =
            Regex(
                """(?:posicion|p)\s*(\d{1,2})\s*(?:valor|es|en)?\s*(.+)"""
            ).find(texto)
                ?: return null

        val posicion =
            coincidencia
                .groupValues[1]
                .toIntOrNull()
                ?: return null

        var valorTexto =
            coincidencia
                .groupValues[2]
                .trim()

        valorTexto = valorTexto
            .replace("milimetros", "")
            .replace("milimetro", "")
            .replace("mm", "")
            .trim()

        if (valorTexto.isBlank()) {
            return null
        }

        return VoiceCommand.ActualizarPosicion(
            posicion = posicion,
            valor = convertirTextoNumerico(
                valorTexto
            )
        )
    }


    private fun construirCodigoVolqueta(
        numero: Int
    ): String {

        return "VVOLQ" +
                numero
                    .toString()
                    .padStart(4, '0')
    }

    private fun construirCodigoCamioneta(
        numero: Int
    ): String {

        return "VCAM" +
                numero
                    .toString()
                    .padStart(4, '0')
    }

    private fun extraerPrimerNumero(
        texto: String
    ): Int? {

        return Regex("""\d+""")
            .find(texto)
            ?.value
            ?.toIntOrNull()
    }

    private fun extraerValorDespuesDe(
        texto: String,
        palabrasClave: List<String>
    ): String? {

        palabrasClave.forEach { palabra ->

            val indice =
                texto.indexOf(palabra)

            if (indice >= 0) {

                val valor =
                    texto
                        .substring(
                            indice + palabra.length
                        )
                        .trim()
                        .removePrefix("es")
                        .trim()
                        .removePrefix("valor")
                        .trim()

                if (valor.isNotBlank()) {
                    return valor
                }
            }
        }

        return null
    }

    private fun normalizarProyecto(
        valor: String
    ): String {

        val proyecto =
            valor
                .trim()
                .lowercase()

        return when {

            proyecto.contains("billonaco") ->
                "Villonaco"

            proyecto.contains("villonaco") ->
                "Villonaco"

            proyecto.contains("viyonaco") ->
                "Villonaco"

            proyecto.contains("biyonaco") ->
                "Villonaco"

            else ->
                valor
                    .trim()
                    .replaceFirstChar {
                        it.uppercase()
                    }
        }
    }

    private fun convertirKilometraje(
        texto: String
    ): String {

        val contieneMil =
            texto
                .lowercase()
                .contains("mil")

        val valorConvertido =
            convertirTextoNumerico(texto)

        val numero =
            valorConvertido
                .toDoubleOrNull()
                ?: return valorConvertido

        val kilometraje =
            if (
                contieneMil &&
                numero in 1.0..999.0
            ) {
                numero * 1000
            } else {
                numero
            }

        return formatearNumero(kilometraje)
    }

    private fun convertirTextoNumerico(
        texto: String
    ): String {

        val limpio =
            texto
                .lowercase()
                .replace(",", ".")
                .replace("coma", " punto ")
                .replace("con", " punto ")
                .replace(
                    Regex(
                        """(\d{1,3})\.\s*(\d{3})(?!\d)"""
                    ),
                    "$1$2"
                )
                .replace(
                    Regex(
                        """(\d{1,3})\s+(\d{3})(?!\d)"""
                    ),
                    "$1$2"
                )
                .trim()

        val numeroConMil =
            Regex(
                """(\d+(?:\.\d+)?)\s*mil"""
            ).find(limpio)

        if (numeroConMil != null) {

            val numeroBase =
                numeroConMil
                    .groupValues[1]
                    .toDoubleOrNull()

            if (numeroBase != null) {
                return formatearNumero(
                    numeroBase * 1000
                )
            }
        }

        limpio
            .toDoubleOrNull()
            ?.let { numero ->

                return formatearNumero(numero)
            }

        val partes =
            limpio.split("punto")

        val entero =
            convertirPalabrasAEntero(
                partes
                    .first()
                    .trim()
            )
                ?: return limpio

        if (partes.size == 1) {
            return entero.toString()
        }

        val parteDecimalTexto =
            partes
                .drop(1)
                .joinToString("")
                .trim()

        val parteDecimal =
            convertirPalabrasAEntero(
                parteDecimalTexto
            )
                ?: return entero.toString()

        return "$entero.$parteDecimal"
    }

    private fun convertirPalabrasAEntero(
        texto: String
    ): Int? {

        texto
            .trim()
            .toIntOrNull()
            ?.let {
                return it
            }

        val unidades =
            mapOf(
                "cero" to 0,
                "uno" to 1,
                "una" to 1,
                "dos" to 2,
                "tres" to 3,
                "cuatro" to 4,
                "cinco" to 5,
                "seis" to 6,
                "siete" to 7,
                "ocho" to 8,
                "nueve" to 9,
                "diez" to 10,
                "once" to 11,
                "doce" to 12,
                "trece" to 13,
                "catorce" to 14,
                "quince" to 15,
                "dieciseis" to 16,
                "diecisiete" to 17,
                "dieciocho" to 18,
                "diecinueve" to 19,
                "veinte" to 20,
                "veintiuno" to 21,
                "veintidos" to 22,
                "veintitres" to 23,
                "veinticuatro" to 24,
                "veinticinco" to 25,
                "veintiseis" to 26,
                "veintisiete" to 27,
                "veintiocho" to 28,
                "veintinueve" to 29
            )

        val decenas =
            mapOf(
                "treinta" to 30,
                "cuarenta" to 40,
                "cincuenta" to 50,
                "sesenta" to 60,
                "setenta" to 70,
                "ochenta" to 80,
                "noventa" to 90
            )

        val centenas =
            mapOf(
                "cien" to 100,
                "ciento" to 100,
                "doscientos" to 200,
                "trescientos" to 300,
                "cuatrocientos" to 400,
                "quinientos" to 500,
                "seiscientos" to 600,
                "setecientos" to 700,
                "ochocientos" to 800,
                "novecientos" to 900
            )

        val palabras =
            texto
                .replace("-", " ")
                .split(" ")
                .filter {
                    it.isNotBlank() &&
                            it != "y"
                }

        if (palabras.isEmpty()) {
            return null
        }

        var total = 0
        var acumulado = 0

        palabras.forEach { palabra ->

            when {

                palabra == "mil" -> {

                    acumulado =
                        if (acumulado == 0) {
                            1
                        } else {
                            acumulado
                        }

                    total += acumulado * 1000
                    acumulado = 0
                }

                centenas.containsKey(palabra) -> {

                    acumulado +=
                        centenas.getValue(palabra)
                }

                decenas.containsKey(palabra) -> {

                    acumulado +=
                        decenas.getValue(palabra)
                }

                unidades.containsKey(palabra) -> {

                    acumulado +=
                        unidades.getValue(palabra)
                }

                else -> {
                    return null
                }
            }
        }

        return total + acumulado
    }

    private fun convertirPalabraNumero(
        palabra: String
    ): Int? {

        return convertirPalabrasAEntero(
            palabra
        )
    }

    private fun normalizarNombre(
        valor: String
    ): String {

        return valor
            .trim()
            .split(" ")
            .filter {
                it.isNotBlank()
            }
            .joinToString(" ") { palabra ->

                palabra.replaceFirstChar { caracter ->
                    caracter.uppercase()
                }
            }
    }

    private fun normalizarTexto(
        texto: String
    ): String {

        val sinTildes =
            Normalizer.normalize(
                texto.lowercase(),
                Normalizer.Form.NFD
            ).replace(
                Regex("\\p{Mn}+"),
                ""
            )

        return sinTildes
            .replace(
                Regex("[^a-z0-9., ]"),
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }

    private fun formatearNumero(
        valor: Double
    ): String {

        return if (
            valor % 1.0 == 0.0
        ) {
            valor
                .toLong()
                .toString()
        } else {
            valor.toString()
        }
    }
}