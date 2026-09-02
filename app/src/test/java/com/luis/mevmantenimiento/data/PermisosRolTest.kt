package com.luis.mevmantenimiento.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermisosRolTest {
    @Test
    fun tecnicoSoloAccedeAlFlujoDeMantenimiento() {
        assertTrue(PermisosRol.puedeAccederPantalla("TECNICO_MECANICO", "NUEVO_MANTENIMIENTO"))
        assertFalse(PermisosRol.puedeAccederPantalla("TECNICO_MECANICO", "REPORTES"))
        assertFalse(PermisosRol.puedeAccederPantalla("TECNICO_MECANICO", "USUARIOS"))
    }

    @Test
    fun vulcanizadorSoloAccedeAlFlujoDeLlantas() {
        assertTrue(PermisosRol.puedeAccederPantalla("VULCANIZADOR", "TOMA_HUELLA"))
        assertTrue(PermisosRol.puedeAccederPantalla("VULCANIZADOR", "INTERVENCION_LLANTA"))
        assertFalse(PermisosRol.puedeAccederPantalla("VULCANIZADOR", "NUEVO_MANTENIMIENTO"))
    }

    @Test
    fun ejecutivosTienenConsultaSinAdministracion() {
        listOf("JEFE_OPERACIONES", "GERENTE_GENERAL").forEach { rol ->
            assertTrue(PermisosRol.puedeAccederPantalla(rol, "REPORTES"))
            assertTrue(PermisosRol.puedeAccederPantalla(rol, "ACTIVOS"))
            assertFalse(PermisosRol.puedeAccederPantalla(rol, "USUARIOS"))
            assertFalse(PermisosRol.puedeAdministrarMatriz(rol))
        }
    }

    @Test
    fun soloPlanificadorAdministraUsuariosYMatriz() {
        assertTrue(PermisosRol.puedeAccederPantalla("PLANIFICADOR", "USUARIOS"))
        assertTrue(PermisosRol.puedeAdministrarMatriz("PLANIFICADOR"))
        assertFalse(PermisosRol.puedeAdministrarMatriz("ASISTENTE_PLANIFICACION"))
    }
}
