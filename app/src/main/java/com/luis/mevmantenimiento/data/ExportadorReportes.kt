package com.luis.mevmantenimiento.data

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.luis.mevmantenimiento.ui.screens.ReporteResumen
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportadorReportes {

    fun exportarPdf(
        outputStream: OutputStream,
        resumen: ReporteResumen
    ) {
        val documento = PdfDocument()
        val paginaInfo = PdfDocument.PageInfo.Builder(
            595,
            842,
            1
        ).create()

        val pagina = documento.startPage(paginaInfo)
        val canvas = pagina.canvas

        val titulo = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }

        val subtitulo = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
        }

        val texto = Paint().apply {
            textSize = 12f
        }

        var y = 60f

        canvas.drawText(
            "MEV - Reporte consolidado",
            40f,
            y,
            titulo
        )

        y += 34f
        canvas.drawText(
            "Resumen de mantenimiento y vulcanización",
            40f,
            y,
            subtitulo
        )

        y += 36f
        canvas.drawText("MANTENIMIENTO", 40f, y, subtitulo)
        y += 24f

        val mantenimiento = listOf(
            "Total de mantenimientos" to resumen.totalMantenimientos,
            "Preventivos" to resumen.preventivos,
            "Correctivos" to resumen.correctivos,
            "Enviados" to resumen.mantenimientosEnviados,
            "Aprobados" to resumen.mantenimientosAprobados,
            "Devueltos" to resumen.mantenimientosDevueltos
        )

        mantenimiento.forEach { (nombre, valor) ->
            canvas.drawText("$nombre: $valor", 55f, y, texto)
            y += 20f
        }

        y += 16f
        canvas.drawText("VULCANIZACIÓN", 40f, y, subtitulo)
        y += 24f

        val vulcanizacion = listOf(
            "Tomas de huella" to resumen.totalHuellas,
            "Huellas críticas (<= 6 mm)" to resumen.huellasCriticas,
            "Total de intervenciones" to resumen.totalIntervenciones,
            "Cambios" to resumen.cambios,
            "Rotaciones" to resumen.rotaciones,
            "Reparaciones" to resumen.reparaciones,
            "Montajes" to resumen.montajes,
            "Desmontajes" to resumen.desmontajes,
            "Bajas" to resumen.bajas
        )

        vulcanizacion.forEach { (nombre, valor) ->
            canvas.drawText("$nombre: $valor", 55f, y, texto)
            y += 20f
        }

        y += 16f
        canvas.drawText(
            "Pendientes de revisión: ${resumen.registrosPendientesRevision}",
            40f,
            y,
            subtitulo
        )

        documento.finishPage(pagina)
        documento.writeTo(outputStream)
        documento.close()
    }

    fun exportarExcel(
        outputStream: OutputStream,
        resumen: ReporteResumen
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
                hojaResumen(resumen)
            )
        }
    }

    private fun hojaResumen(
        resumen: ReporteResumen
    ): String {
        val filas = listOf(
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

        val contenidoFilas = filas.mapIndexed { indice, fila ->
            val numeroFila = indice + 1

            val celdaA = """
                <c r="A$numeroFila" t="inlineStr">
                    <is><t>${escaparXml(fila[0])}</t></is>
                </c>
            """.trimIndent()

            val celdaB = """
                <c r="B$numeroFila">
                    <v>${fila[1]}</v>
                </c>
            """.trimIndent()

            """
                <row r="$numeroFila">
                    $celdaA
                    $celdaB
                </row>
            """.trimIndent()
        }.joinToString("\n")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
        $contenidoFilas
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
</Relationships>
"""
    }

    private fun agregarEntrada(
        zip: ZipOutputStream,
        ruta: String,
        contenido: String
    ) {
        zip.putNextEntry(ZipEntry(ruta))
        zip.write(contenido.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
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