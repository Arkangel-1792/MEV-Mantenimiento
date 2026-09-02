package com.luis.mevmantenimiento.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuariosValidationTest {
    private val datosValidos = DatosUsuario(
        nombres = "María",
        apellidos = "Docente",
        email = "maria.docente@example.com",
        cargo = "Docente evaluadora",
        rol = "GERENTE_GENERAL",
        estadoUsuario = "ACTIVO",
        passwordTemporal = "Temporal2026"
    )

    @Test
    fun aceptaLosOchoRolesConfigurados() {
        UsuariosValidation.roles.forEach { rol ->
            assertNull(
                UsuariosValidation.validar(
                    datosValidos.copy(rol = rol),
                    requierePassword = true
                )
            )
        }
        assertEquals(8, UsuariosValidation.roles.size)
    }

    @Test
    fun exigePasswordTemporalSoloAlCrear() {
        val sinPassword = datosValidos.copy(passwordTemporal = "")
        assertEquals(
            "La contraseña temporal debe tener al menos 6 caracteres.",
            UsuariosValidation.validar(sinPassword, requierePassword = true)
        )
        assertNull(UsuariosValidation.validar(sinPassword, requierePassword = false))
    }

    @Test
    fun rechazaCorreoYEstadoInvalidos() {
        assertEquals(
            "El correo electrónico no tiene un formato válido.",
            UsuariosValidation.validar(
                datosValidos.copy(email = "correo-invalido"),
                requierePassword = true
            )
        )
        assertEquals(
            "El estado seleccionado no es válido.",
            UsuariosValidation.validar(
                datosValidos.copy(estadoUsuario = "PENDIENTE"),
                requierePassword = true
            )
        )
    }
}
