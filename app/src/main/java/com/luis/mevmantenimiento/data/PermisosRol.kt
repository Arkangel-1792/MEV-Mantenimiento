package com.luis.mevmantenimiento.data

object PermisosRol {
    private val pantallasComunes = setOf("MENU")

    private val pantallasMantenimiento = setOf(
        "NUEVO_MANTENIMIENTO",
        "MIS_BORRADORES",
        "EDITAR_BORRADOR",
        "MI_HISTORIAL"
    )

    private val pantallasVulcanizacion = setOf(
        "TOMA_HUELLA",
        "INTERVENCION_LLANTA",
        "MIS_BORRADORES",
        "EDITAR_BORRADOR_HUELLA",
        "EDITAR_BORRADOR_INTERVENCION",
        "MIS_BORRADORES_HUELLA",
        "MIS_BORRADORES_INTERVENCION",
        "MI_HISTORIAL",
        "HISTORIAL_HUELLA",
        "HISTORIAL_INTERVENCION"
    )

    private val pantallasRevision = setOf(
        "REVISION_REGISTROS",
        "REVISION_HUELLA",
        "REVISION_INTERVENCION",
        "REGISTROS_APROBADOS"
    )

    private val pantallasMatrizConsulta = setOf(
        "MATRIZ_BASE",
        "ACTIVOS",
        "DETALLE_ACTIVO"
    )

    private val permitidasPorRol = mapOf(
        "TECNICO_MECANICO" to pantallasMantenimiento,
        "VULCANIZADOR" to pantallasVulcanizacion,
        "SUPERVISOR_MANTENIMIENTO" to (
            setOf("NUEVO_MANTENIMIENTO", "TOMA_HUELLA", "INTERVENCION_LLANTA") +
                pantallasRevision
            ),
        "ANALISTA_MANTENIMIENTO" to (
            setOf("NUEVO_MANTENIMIENTO", "TOMA_HUELLA", "INTERVENCION_LLANTA", "REPORTES") +
                pantallasRevision
            ),
        "ASISTENTE_PLANIFICACION" to (
            pantallasMantenimiento + pantallasVulcanizacion + pantallasRevision + "REPORTES"
            ),
        "PLANIFICADOR" to (
            pantallasMantenimiento + pantallasVulcanizacion + pantallasRevision +
                pantallasMatrizConsulta + setOf("REPORTES", "USUARIOS")
            ),
        "JEFE_OPERACIONES" to (pantallasMatrizConsulta + "REPORTES"),
        "GERENTE_GENERAL" to (pantallasMatrizConsulta + "REPORTES")
    )

    fun puedeAccederPantalla(rol: String, pantalla: String): Boolean {
        val rolNormalizado = rol.trim().uppercase()
        return pantalla in pantallasComunes ||
            pantalla in permitidasPorRol[rolNormalizado].orEmpty()
    }

    fun puedeAdministrarMatriz(rol: String): Boolean =
        rol.trim().uppercase() == "PLANIFICADOR"
}
