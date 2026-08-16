package com.luis.mevmantenimiento.voice

import java.text.Normalizer

object VoiceCommandParser {

    fun interpretar(
        textoOriginal: String
    ): VoiceCommand {

        val texto = normalizarTexto(textoOriginal)

        if (
            texto.contains("guardar borrador") ||
            texto == "guardar" ||
            texto == "guardar registro"
        ) {
            return VoiceCommand.GuardarBorrador
        }

        if (
            texto.contains("enviar registro") ||
            texto == "enviar" ||
            texto == "enviar borrador"
        ) {
            return VoiceCommand.EnviarRegistro
        }

        interpretarActivo(texto)?.let {
            return it
        }

        // Toma general de huella: "posición 10 7.5 milímetros"
        interpretarPosicionConHuella(texto)?.let {
            return it
        }

        // Intervención de llanta: "posición 10"
        interpretarPosicionLlanta(texto)?.let {
            return it
        }

        extraerValorDespuesDe(
            texto,
            listOf("proyecto", "obra", "campamento")
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
                "kilometro",
                "km"
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
                "hora equipo",
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
                "tipo de servicio",
                "servicio"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarTipoServicio(
                valor = normalizarTipoServicio(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "accion ejecutada",
                "accion realizada",
                "trabajo realizado",
                "trabajo ejecutado",
                "actividad realizada",
                "accion"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarAccionEjecutada(
                valor = valor.trim()
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "orden de trabajo",
                "numero de orden",
                "orden"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarOrdenTrabajo(
                valor = limpiarIdentificador(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "numero de pedido",
                "numero pedido",
                "pedido"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarNumeroPedido(
                valor = limpiarIdentificador(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "tipo de intervencion",
                "intervencion"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarTipoIntervencion(
                valor = normalizarTipoIntervencion(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "huella de llanta",
                "profundidad de huella",
                "profundidad",
                "huella"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarHuellaLlanta(
                valor = convertirMedidaMilimetros(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "marca de llanta",
                "marca llanta",
                "marca"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarMarcaLlanta(
                valor = normalizarTextoLibre(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "medida de llanta",
                "medida llanta",
                "dimension de llanta",
                "dimension",
                "medida"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarMedidaLlanta(
                valor = normalizarMedidaLlanta(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "serie de llanta",
                "serie llanta",
                "numero de serie",
                "serie"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarSerieLlanta(
                valor = limpiarIdentificador(valor)
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "motivo de intervencion",
                "motivo"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarMotivoIntervencion(
                valor = valor.trim()
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "estado general",
                "condicion general",
                "estado"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarEstadoGeneral(
                valor = valor.trim()
            )
        }

        extraerValorDespuesDe(
            texto,
            listOf(
                "novedad",
                "observaciones",
                "observacion"
            )
        )?.let { valor ->
            return VoiceCommand.ActualizarNovedad(
                valor = valor.trim()
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

        val patronInicio = Regex(
            """\b(?:seleccionar\s+activo|activo|volqueta|camioneta|equipo|proyecto|obra|campamento|kilometraje|kilometros?|km|horometro|orometro|odometro|contador\s+de\s+horas|horas\s+del\s+equipo|hora\s+equipo|tipo\s+de\s+servicio|servicio|accion\s+ejecutada|accion\s+realizada|trabajo\s+realizado|trabajo\s+ejecutado|actividad\s+realizada|accion|orden\s+de\s+trabajo|numero\s+de\s+orden|orden|numero\s+de\s+pedido|numero\s+pedido|pedido|tipo\s+de\s+intervencion|intervencion|huella\s+de\s+llanta|profundidad\s+de\s+huella|profundidad|huella|marca\s+de\s+llanta|marca\s+llanta|marca|medida\s+de\s+llanta|medida\s+llanta|dimension\s+de\s+llanta|dimension|medida|serie\s+de\s+llanta|serie\s+llanta|numero\s+de\s+serie|serie|motivo\s+de\s+intervencion|motivo|estado\s+general|condicion\s+general|estado|novedad|observaciones?|nombre\s+del\s+tecnico|nombre\s+tecnico|tecnico|vulcanizador|mecanico|posicion\s+(?:\d{1,2}|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce)|p\s*(?:\d{1,2}|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce)|guardar\s+borrador|guardar\s+registro|enviar\s+registro|enviar\s+borrador)\b"""
        )

        val inicios = patronInicio
            .findAll(texto)
            .map { it.range.first }
            .distinct()
            .toList()

        if (inicios.isEmpty()) {
            return listOf(
                interpretar(textoOriginal)
            )
        }

        val segmentos = mutableListOf<String>()

        inicios.forEachIndexed { indice, inicio ->
            val fin =
                if (indice < inicios.lastIndex) {
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

        val codigoVolqueta = Regex(
            """vvolq\s*0*(\d+)"""
        ).find(texto)

        if (codigoVolqueta != null) {
            val numero = codigoVolqueta
                .groupValues[1]
                .toIntOrNull()
                ?: return null

            return VoiceCommand.SeleccionarActivo(
                codigo = construirCodigoVolqueta(numero)
            )
        }

        val codigoCamioneta = Regex(
            """vcam\s*0*(\d+)"""
        ).find(texto)

        if (codigoCamioneta != null) {
            val numero = codigoCamioneta
                .groupValues[1]
                .toIntOrNull()
                ?: return null

            return VoiceCommand.SeleccionarActivo(
                codigo = construirCodigoCamioneta(numero)
            )
        }

        val numero =
            extraerPrimerNumero(texto)
                ?: convertirPalabrasAEntero(
                    texto
                        .substringAfter("volqueta", "")
                        .ifBlank {
                            texto.substringAfter("camioneta", "")
                        }
                        .ifBlank {
                            texto.substringAfter("activo", "")
                        }
                        .ifBlank {
                            texto.substringAfter("equipo", "")
                        }
                        .trim()
                )
                ?: return null

        return when {
            texto.contains("volqueta") ->
                VoiceCommand.SeleccionarActivo(
                    codigo = construirCodigoVolqueta(numero)
                )

            texto.contains("camioneta") ->
                VoiceCommand.SeleccionarActivo(
                    codigo = construirCodigoCamioneta(numero)
                )

            else ->
                VoiceCommand.SeleccionarActivo(
                    codigo = numero.toString()
                )
        }
    }

    private fun interpretarPosicionConHuella(
        texto: String
    ): VoiceCommand.ActualizarPosicion? {

        if (
            !texto.contains("posicion") &&
            !Regex(
                """\bp\s*(?:\d{1,2}|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce)\b"""
            ).containsMatchIn(texto)
        ) {
            return null
        }

        val coincidencia = Regex(
            """(?:posicion|p)\s*(\d{1,2}|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce)\s*(?:valor|es|en)?\s*(.+)"""
        ).find(texto)
            ?: return null

        val posicion = convertirPalabrasAEntero(
            coincidencia.groupValues[1]
        ) ?: return null

        if (posicion !in 1..12) {
            return null
        }

        val valorTexto = limpiarUnidadMilimetros(
            coincidencia.groupValues[2]
        )

        if (valorTexto.isBlank()) {
            return null
        }

        val valor = convertirTextoNumerico(valorTexto)

        if (valor.toDoubleOrNull() == null) {
            return null
        }

        return VoiceCommand.ActualizarPosicion(
            posicion = posicion,
            valor = valor
        )
    }

    private fun interpretarPosicionLlanta(
        texto: String
    ): VoiceCommand.ActualizarPosicionLlanta? {

        val coincidencia = Regex(
            """^(?:posicion|p)\s*(\d{1,2}|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce)\s*$"""
        ).find(texto)
            ?: return null

        val posicion = convertirPalabrasAEntero(
            coincidencia.groupValues[1]
        ) ?: return null

        if (posicion !in 1..12) {
            return null
        }

        return VoiceCommand.ActualizarPosicionLlanta(
            posicion = posicion
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

        palabrasClave
            .sortedByDescending { it.length }
            .forEach { palabra ->

                val coincidencia = Regex(
                    """(?:^|\b)${Regex.escape(palabra)}\b"""
                ).find(texto)

                if (coincidencia != null) {
                    val valor = texto
                        .substring(coincidencia.range.last + 1)
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

    private fun normalizarTipoServicio(
        valor: String
    ): String {
        val limpio = valor.lowercase().trim()

        return when {
            limpio.contains("preventivo") ||
                    limpio.contains("preventiva") ->
                "PREVENTIVO"

            limpio.contains("correctivo") ||
                    limpio.contains("correctiva") ->
                "CORRECTIVO"

            else ->
                valor.trim().uppercase()
        }
    }

    private fun normalizarTipoIntervencion(
        valor: String
    ): String {
        val limpio = valor.lowercase().trim()

        return when {
            limpio.contains("rotacion") ||
                    limpio.contains("rotar") ->
                "ROTACIÓN"

            limpio.contains("reparacion") ||
                    limpio.contains("reparar") ||
                    limpio.contains("parche") ->
                "REPARACIÓN"

            limpio.contains("desmontaje") ||
                    limpio.contains("desmontar") ->
                "DESMONTAJE"

            limpio.contains("montaje") ||
                    limpio.contains("montar") ->
                "MONTAJE"

            limpio.contains("baja") ||
                    limpio.contains("dar de baja") ->
                "BAJA"

            limpio.contains("cambio") ||
                    limpio.contains("cambiar") ||
                    limpio.contains("reemplazo") ->
                "CAMBIO"

            else ->
                valor.trim().uppercase()
        }
    }

    private fun convertirMedidaMilimetros(
        valor: String
    ): String {
        return convertirTextoNumerico(
            limpiarUnidadMilimetros(valor)
        )
    }

    private fun limpiarUnidadMilimetros(
        valor: String
    ): String {
        return valor
            .lowercase()
            .replace("milimetros", "")
            .replace("milimetro", "")
            .replace("milímetros", "")
            .replace("milímetro", "")
            .replace(
                Regex("""\bmm\b"""),
                ""
            )
            .trim()
    }

    private fun limpiarIdentificador(
        valor: String
    ): String {
        return valor
            .trim()
            .replace(
                Regex("""\s+"""),
                ""
            )
            .uppercase()
    }

    private fun normalizarTextoLibre(
        valor: String
    ): String {
        return valor
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { palabra ->
                palabra.replaceFirstChar { caracter ->
                    caracter.uppercase()
                }
            }
    }

    private fun normalizarMedidaLlanta(
        valor: String
    ): String {
        return valor
            .trim()
            .uppercase()
            .replace(" POR ", "X")
            .replace(
                Regex("""\s+R\s+"""),
                "R"
            )
            .replace(
                Regex("""\s+"""),
                " "
            )
    }

    private fun normalizarProyecto(
        valor: String
    ): String {
        val proyecto = valor
            .trim()
            .lowercase()

        return when {
            proyecto.contains("billonaco") ||
                    proyecto.contains("villonaco") ||
                    proyecto.contains("viyonaco") ||
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

        val limpio = texto
            .lowercase()
            .replace("kilometros", "")
            .replace("kilometro", "")
            .replace(
                Regex("""\bkm\b"""),
                ""
            )
            .trim()

        val valorConvertido = convertirTextoNumerico(
            limpio
        )

        val numero = valorConvertido
            .toDoubleOrNull()
            ?: return valorConvertido

        /*
         * No inferimos "mil" si el usuario no lo dijo.
         * Esto evita convertir accidentalmente 125 km en 125000 km.
         */
        return formatearNumero(numero)
    }

    private fun convertirTextoNumerico(
        texto: String
    ): String {

        val limpio = texto
            .lowercase()
            .replace(",", ".")
            .replace("coma", " punto ")
            .replace("con", " punto ")
            .replace(
                Regex("""(\d{1,3})\.\s*(\d{3})(?!\d)"""),
                "$1$2"
            )
            .replace(
                Regex("""(\d{1,3})\s+(\d{3})(?!\d)"""),
                "$1$2"
            )
            .replace(
                Regex("""\s+"""),
                " "
            )
            .trim()

        val numeroConMil = Regex(
            """(\d+(?:\.\d+)?)\s*mil"""
        ).find(limpio)

        if (numeroConMil != null) {
            val numeroBase = numeroConMil
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

        val partes = limpio.split("punto")

        val parteEnteraTexto = partes
            .first()
            .trim()

        val entero = convertirPalabrasAEntero(
            parteEnteraTexto
        ) ?: return limpio

        if (partes.size == 1) {
            return entero.toString()
        }

        val decimalTexto = partes
            .drop(1)
            .joinToString(" ")
            .trim()

        val decimal = convertirParteDecimal(
            decimalTexto
        ) ?: return entero.toString()

        return "$entero.$decimal"
    }

    private fun convertirParteDecimal(
        texto: String
    ): String? {

        val limpio = texto.trim()

        if (limpio.isBlank()) {
            return null
        }

        limpio
            .replace(" ", "")
            .takeIf { it.all(Char::isDigit) }
            ?.let {
                return it
            }

        val digitos = mapOf(
            "cero" to "0",
            "uno" to "1",
            "una" to "1",
            "dos" to "2",
            "tres" to "3",
            "cuatro" to "4",
            "cinco" to "5",
            "seis" to "6",
            "siete" to "7",
            "ocho" to "8",
            "nueve" to "9"
        )

        val palabras = limpio
            .split(" ")
            .filter { it.isNotBlank() }

        if (
            palabras.isNotEmpty() &&
            palabras.all { digitos.containsKey(it) }
        ) {
            return palabras.joinToString("") {
                digitos.getValue(it)
            }
        }

        return convertirPalabrasAEntero(limpio)
            ?.toString()
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

        val unidades = mapOf(
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

        val decenas = mapOf(
            "treinta" to 30,
            "cuarenta" to 40,
            "cincuenta" to 50,
            "sesenta" to 60,
            "setenta" to 70,
            "ochenta" to 80,
            "noventa" to 90
        )

        val centenas = mapOf(
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

        val palabras = texto
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
                    acumulado += centenas.getValue(palabra)
                }

                decenas.containsKey(palabra) -> {
                    acumulado += decenas.getValue(palabra)
                }

                unidades.containsKey(palabra) -> {
                    acumulado += unidades.getValue(palabra)
                }

                else -> {
                    return null
                }
            }
        }

        return total + acumulado
    }

    private fun normalizarNombre(
        valor: String
    ): String {
        return valor
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { palabra ->
                palabra.replaceFirstChar { caracter ->
                    caracter.uppercase()
                }
            }
    }

    private fun normalizarTexto(
        texto: String
    ): String {

        val sinTildes = Normalizer.normalize(
            texto.lowercase(),
            Normalizer.Form.NFD
        ).replace(
            Regex("\\p{Mn}+"),
            ""
        )

        return sinTildes
            .replace(
                Regex("[^a-z0-9.,/ ]"),
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
        return if (valor % 1.0 == 0.0) {
            valor
                .toLong()
                .toString()
        } else {
            valor.toString()
        }
    }
}