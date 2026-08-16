package com.luis.mevmantenimiento.data

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.google.firebase.Timestamp
import com.luis.mevmantenimiento.ui.screens.ReporteResumen
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportadorReportes {

    fun exportarPdf(
        outputStream: OutputStream,
        reporte: ReporteDatos
    ) {
        val documento = PdfDocument()

        try {
            crearPaginaResumen(
                documento = documento,
                resumen = reporte.resumen
            )

            crearPaginasDetalle(
                documento = documento,
                tituloSeccion = "Mantenimientos",
                encabezados = listOf(
                    "Activo",
                    "Servicio",
                    "Estado",
                    "Lectura",
                    "Acción"
                ),
                filas = reporte.mantenimientos.map { registro ->
                    listOf(
                        texto(registro["codigoActivo"]),
                        texto(registro["tipoServicio"]),
                        texto(registro["estadoRegistro"]),
                        lectura(registro),
                        texto(registro["accionEjecutada"])
                    )
                }
            )

            crearPaginasDetalle(
                documento = documento,
                tituloSeccion = "Tomas de huella",
                encabezados = listOf(
                    "Activo",
                    "Proyecto",
                    "Estado",
                    "Mediciones",
                    "Técnico"
                ),
                filas = reporte.huellas.map { registro ->
                    listOf(
                        texto(registro["codigoActivo"]),
                        texto(registro["proyecto"]),
                        texto(registro["estadoRegistro"]),
                        contarHuellas(registro).toString(),
                        texto(registro["nombreTecnico"])
                    )
                }
            )

            crearPaginasDetalle(
                documento = documento,
                tituloSeccion = "Intervenciones de llanta",
                encabezados = listOf(
                    "Activo",
                    "Intervención",
                    "Posición",
                    "Huella",
                    "Estado"
                ),
                filas = reporte.intervenciones.map { registro ->
                    listOf(
                        texto(registro["codigoActivo"]),
                        texto(registro["tipoIntervencion"]),
                        texto(registro["posicion"]),
                        (registro["huella"] as? Number)
                            ?.toDouble()
                            ?.let { "$it mm" }
                            ?: "",
                        texto(registro["estadoRegistro"])
                    )
                }
            )

            documento.writeTo(outputStream)
        } finally {
            documento.close()
        }
    }

    fun exportarExcel(
        outputStream: OutputStream,
        reporte: ReporteDatos
    ) {
        ZipOutputStream(outputStream).use { zip ->
            agregarEntrada(
                zip,
                "[Content_Types].xml",
                contenidoTipos()
            )

            agregarEntrada(
                zip,
                "_rels/.rels",
                relacionesRaiz()
            )

            agregarEntrada(
                zip,
                "xl/workbook.xml",
                workbook()
            )

            agregarEntrada(
                zip,
                "xl/_rels/workbook.xml.rels",
                relacionesWorkbook()
            )

            agregarEntrada(
                zip,
                "xl/worksheets/sheet1.xml",
                crearHojaXml(
                    filasResumen(reporte.resumen)
                )
            )

            agregarEntrada(
                zip,
                "xl/worksheets/sheet2.xml",
                crearHojaXml(
                    filasMantenimiento(reporte.mantenimientos)
                )
            )

            agregarEntrada(
                zip,
                "xl/worksheets/sheet3.xml",
                crearHojaXml(
                    filasHuellas(reporte.huellas)
                )
            )

            agregarEntrada(
                zip,
                "xl/worksheets/sheet4.xml",
                crearHojaXml(
                    filasIntervenciones(reporte.intervenciones)
                )
            )
        }
    }

    private fun crearPaginaResumen(
        documento: PdfDocument,
        resumen: ReporteResumen
    ) {
        val numeroPagina = documento.pages.size + 1

        val info = PdfDocument.PageInfo.Builder(
            595,
            842,
            numeroPagina
        ).create()

        val pagina = documento.startPage(info)
        val canvas = pagina.canvas

        val titulo = Paint().apply {
            textSize = 21f
            isFakeBoldText = true
        }

        val subtitulo = Paint().apply {
            textSize = 13f
        }

        val seccion = Paint().apply {
            textSize = 15f
            isFakeBoldText = true
        }

        val texto = Paint().apply {
            textSize = 12f
        }

        var y = 55f

        canvas.drawText(
            "MEV - Reporte Ejecutivo",
            40f,
            y,
            titulo
        )

        y += 26f
        canvas.drawText(
            "Resumen consolidado de mantenimiento y vulcanización",
            40f,
            y,
            subtitulo
        )

        y += 38f
        canvas.drawText(
            "Mantenimiento",
            40f,
            y,
            seccion
        )

        y += 25f

        val mantenimiento = listOf(
            "Total de mantenimientos" to resumen.totalMantenimientos,
            "Preventivos" to resumen.preventivos,
            "Correctivos" to resumen.correctivos,
            "En revisión" to resumen.mantenimientosEnviados,
            "Aprobados" to resumen.mantenimientosAprobados,
            "Devueltos" to resumen.mantenimientosDevueltos
        )

        mantenimiento.forEach { (nombre, valor) ->
            canvas.drawText(
                "$nombre: $valor",
                55f,
                y,
                texto
            )
            y += 20f
        }

        y += 14f
        canvas.drawText(
            "Vulcanización",
            40f,
            y,
            seccion
        )

        y += 25f

        val vulcanizacion = listOf(
            "Tomas de huella" to resumen.totalHuellas,
            "Mediciones críticas (<= 6 mm)" to resumen.huellasCriticas,
            "Intervenciones" to resumen.totalIntervenciones,
            "Cambios" to resumen.cambios,
            "Rotaciones" to resumen.rotaciones,
            "Reparaciones" to resumen.reparaciones,
            "Montajes" to resumen.montajes,
            "Desmontajes" to resumen.desmontajes,
            "Bajas" to resumen.bajas
        )

        vulcanizacion.forEach { (nombre, valor) ->
            canvas.drawText(
                "$nombre: $valor",
                55f,
                y,
                texto
            )
            y += 20f
        }

        y += 18f
        canvas.drawText(
            "Pendientes totales de revisión: " +
                    resumen.registrosPendientesRevision,
            40f,
            y,
            seccion
        )

        documento.finishPage(pagina)
    }

    private fun crearPaginasDetalle(
        documento: PdfDocument,
        tituloSeccion: String,
        encabezados: List<String>,
        filas: List<List<String>>
    ) {
        val filasPorPagina = 24

        if (filas.isEmpty()) {
            crearPaginaTabla(
                documento = documento,
                tituloSeccion = tituloSeccion,
                encabezados = encabezados,
                filas = emptyList()
            )
            return
        }

        filas.chunked(filasPorPagina).forEach { bloque ->
            crearPaginaTabla(
                documento = documento,
                tituloSeccion = tituloSeccion,
                encabezados = encabezados,
                filas = bloque
            )
        }
    }

    private fun crearPaginaTabla(
        documento: PdfDocument,
        tituloSeccion: String,
        encabezados: List<String>,
        filas: List<List<String>>
    ) {
        val numeroPagina = documento.pages.size + 1

        val info = PdfDocument.PageInfo.Builder(
            842,
            595,
            numeroPagina
        ).create()

        val pagina = documento.startPage(info)
        val canvas = pagina.canvas

        val titulo = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        val encabezado = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
        }

        val texto = Paint().apply {
            textSize = 9f
        }

        canvas.drawText(
            tituloSeccion,
            30f,
            35f,
            titulo
        )

        val anchoUtil = 782f
        val anchoColumna = anchoUtil / encabezados.size
        var y = 65f

        encabezados.forEachIndexed { indice, valor ->
            canvas.drawText(
                recortar(valor, 18),
                30f + indice * anchoColumna,
                y,
                encabezado
            )
        }

        y += 18f

        if (filas.isEmpty()) {
            canvas.drawText(
                "Sin registros.",
                30f,
                y + 10f,
                texto
            )
        } else {
            filas.forEach { fila ->
                fila.forEachIndexed { indice, valor ->
                    canvas.drawText(
                        recortar(valor, 24),
                        30f + indice * anchoColumna,
                        y,
                        texto
                    )
                }

                y += 18f
            }
        }

        documento.finishPage(pagina)
    }

    private fun filasResumen(
        resumen: ReporteResumen
    ): List<List<String>> {
        return listOf(
            listOf("Indicador", "Valor"),
            listOf("Total mantenimientos", resumen.totalMantenimientos.toString()),
            listOf("Preventivos", resumen.preventivos.toString()),
            listOf("Correctivos", resumen.correctivos.toString()),
            listOf("Mantenimientos enviados", resumen.mantenimientosEnviados.toString()),
            listOf("Mantenimientos aprobados", resumen.mantenimientosAprobados.toString()),
            listOf("Mantenimientos devueltos", resumen.mantenimientosDevueltos.toString()),
            listOf("Tomas de huella", resumen.totalHuellas.toString()),
            listOf("Huellas críticas <= 6 mm", resumen.huellasCriticas.toString()),
            listOf("Total intervenciones", resumen.totalIntervenciones.toString()),
            listOf("Cambios", resumen.cambios.toString()),
            listOf("Rotaciones", resumen.rotaciones.toString()),
            listOf("Reparaciones", resumen.reparaciones.toString()),
            listOf("Montajes", resumen.montajes.toString()),
            listOf("Desmontajes", resumen.desmontajes.toString()),
            listOf("Bajas", resumen.bajas.toString()),
            listOf("Pendientes de revisión", resumen.registrosPendientesRevision.toString())
        )
    }

    private fun filasMantenimiento(
        registros: List<Map<String, Any?>>
    ): List<List<String>> {
        val encabezado = listOf(
            "ID",
            "Activo",
            "Tipo servicio",
            "Kilometraje",
            "Horómetro",
            "Acción ejecutada",
            "Observaciones",
            "Orden trabajo",
            "Pedido",
            "Estado",
            "Usuario",
            "Fecha creación",
            "Fecha envío",
            "Fecha aprobación",
            "Motivo devolución"
        )

        return listOf(encabezado) +
                registros.map { registro ->
                    listOf(
                        texto(registro["id"]),
                        texto(registro["codigoActivo"]),
                        texto(registro["tipoServicio"]),
                        numero(registro["kilometraje"]),
                        numero(registro["horometro"]),
                        texto(registro["accionEjecutada"]),
                        texto(registro["observaciones"]),
                        texto(registro["ordenTrabajo"]),
                        texto(registro["numeroPedido"]),
                        texto(registro["estadoRegistro"]),
                        texto(registro["emailUsuario"]),
                        fecha(registro["fechaCreacion"]),
                        fecha(registro["fechaEnvio"]),
                        fecha(registro["fechaAprobacion"]),
                        texto(registro["motivoDevolucion"])
                    )
                }
    }

    private fun filasHuellas(
        registros: List<Map<String, Any?>>
    ): List<List<String>> {
        val encabezado = mutableListOf(
            "ID",
            "Activo",
            "Proyecto",
            "Kilometraje",
            "Horómetro"
        )

        (1..12).forEach { posicion ->
            encabezado.add("P$posicion")
        }

        encabezado.addAll(
            listOf(
                "Estado general",
                "Novedad",
                "Técnico",
                "Estado",
                "Usuario",
                "Fecha creación",
                "Fecha envío",
                "Fecha aprobación",
                "Motivo devolución"
            )
        )

        return listOf(encabezado) +
                registros.map { registro ->
                    val fila = mutableListOf(
                        texto(registro["id"]),
                        texto(registro["codigoActivo"]),
                        texto(registro["proyecto"]),
                        numero(registro["kilometraje"]),
                        numero(registro["horometro"])
                    )

                    (1..12).forEach { posicion ->
                        fila.add(
                            numero(registro["P$posicion"])
                        )
                    }

                    fila.addAll(
                        listOf(
                            texto(registro["estadoGeneral"]),
                            texto(registro["novedad"]),
                            texto(registro["nombreTecnico"]),
                            texto(registro["estadoRegistro"]),
                            texto(registro["emailUsuario"]),
                            fecha(registro["fechaCreacion"]),
                            fecha(registro["fechaEnvio"]),
                            fecha(registro["fechaAprobacion"]),
                            texto(registro["motivoDevolucion"])
                        )
                    )

                    fila
                }
    }

    private fun filasIntervenciones(
        registros: List<Map<String, Any?>>
    ): List<List<String>> {
        val encabezado = listOf(
            "ID",
            "Activo",
            "Proyecto",
            "Kilometraje",
            "Horómetro",
            "Intervención",
            "Posición",
            "Huella",
            "Marca",
            "Medida",
            "Serie",
            "Motivo",
            "Observaciones",
            "Técnico",
            "Estado",
            "Usuario",
            "Fecha creación",
            "Fecha envío",
            "Fecha aprobación",
            "Motivo devolución"
        )

        return listOf(encabezado) +
                registros.map { registro ->
                    listOf(
                        texto(registro["id"]),
                        texto(registro["codigoActivo"]),
                        texto(registro["proyecto"]),
                        numero(registro["kilometraje"]),
                        numero(registro["horometro"]),
                        texto(registro["tipoIntervencion"]),
                        texto(registro["posicion"]),
                        numero(registro["huella"]),
                        texto(registro["marcaLlanta"]),
                        texto(registro["medidaLlanta"]),
                        texto(registro["serieLlanta"]),
                        texto(registro["motivo"]),
                        texto(registro["observaciones"]),
                        texto(registro["nombreTecnico"]),
                        texto(registro["estadoRegistro"]),
                        texto(registro["emailUsuario"]),
                        fecha(registro["fechaCreacion"]),
                        fecha(registro["fechaEnvio"]),
                        fecha(registro["fechaAprobacion"]),
                        texto(registro["motivoDevolucion"])
                    )
                }
    }

    private fun crearHojaXml(
        filas: List<List<String>>
    ): String {
        val contenido = filas.mapIndexed { indiceFila, fila ->
            val numeroFila = indiceFila + 1

            val celdas = fila.mapIndexed { indiceColumna, valor ->
                val referencia =
                    columnaExcel(indiceColumna + 1) + numeroFila

                """
                <c r="$referencia" t="inlineStr">
                    <is><t>${escaparXml(valor)}</t></is>
                </c>
                """.trimIndent()
            }.joinToString("\n")

            """
            <row r="$numeroFila">
                $celdas
            </row>
            """.trimIndent()
        }.joinToString("\n")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
        $contenido
    </sheetData>
</worksheet>
"""
    }

    private fun contenidoTipos(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>
"""
    }

    private fun relacionesRaiz(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship
        Id="rId1"
        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
        Target="xl/workbook.xml"/>
</Relationships>
"""
    }

    private fun workbook(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="Resumen" sheetId="1" r:id="rId1"/>
        <sheet name="Mantenimientos" sheetId="2" r:id="rId2"/>
        <sheet name="Tomas de Huella" sheetId="3" r:id="rId3"/>
        <sheet name="Intervenciones" sheetId="4" r:id="rId4"/>
    </sheets>
</workbook>
"""
    }

    private fun relacionesWorkbook(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship
        Id="rId1"
        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
        Target="worksheets/sheet1.xml"/>
    <Relationship
        Id="rId2"
        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
        Target="worksheets/sheet2.xml"/>
    <Relationship
        Id="rId3"
        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
        Target="worksheets/sheet3.xml"/>
    <Relationship
        Id="rId4"
        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
        Target="worksheets/sheet4.xml"/>
</Relationships>
"""
    }

    private fun agregarEntrada(
        zip: ZipOutputStream,
        ruta: String,
        contenido: String
    ) {
        zip.putNextEntry(
            ZipEntry(ruta)
        )

        zip.write(
            contenido.toByteArray(Charsets.UTF_8)
        )

        zip.closeEntry()
    }

    private fun columnaExcel(
        numero: Int
    ): String {
        var n = numero
        val resultado = StringBuilder()

        while (n > 0) {
            val resto = (n - 1) % 26
            resultado.insert(
                0,
                ('A'.code + resto).toChar()
            )
            n = (n - 1) / 26
        }

        return resultado.toString()
    }

    private fun texto(
        valor: Any?
    ): String {
        return valor
            ?.toString()
            .orEmpty()
    }

    private fun numero(
        valor: Any?
    ): String {
        val numero =
            (valor as? Number)?.toDouble()
                ?: return ""

        return if (numero % 1.0 == 0.0) {
            numero.toLong().toString()
        } else {
            numero.toString()
        }
    }

    private fun fecha(
        valor: Any?
    ): String {
        val timestamp =
            valor as? Timestamp
                ?: return ""

        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        return formato.format(
            timestamp.toDate()
        )
    }

    private fun lectura(
        registro: Map<String, Any?>
    ): String {
        val km = numero(
            registro["kilometraje"]
        )

        val hr = numero(
            registro["horometro"]
        )

        return when {
            km.isNotBlank() && hr.isNotBlank() ->
                "$km km / $hr h"

            km.isNotBlank() ->
                "$km km"

            hr.isNotBlank() ->
                "$hr h"

            else ->
                ""
        }
    }

    private fun contarHuellas(
        registro: Map<String, Any?>
    ): Int {
        return (1..12).count { posicion ->
            registro["P$posicion"] is Number
        }
    }

    private fun recortar(
        texto: String,
        maximo: Int
    ): String {
        if (texto.length <= maximo) {
            return texto
        }

        return texto.take(
            maximo - 3
        ) + "..."
    }

    private fun escaparXml(
        texto: String
    ): String {
        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}