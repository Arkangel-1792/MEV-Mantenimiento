package com.luis.mevmantenimiento.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HuellaValidationTest {
    @Test fun aceptaLimiteDe26() = assertTrue(aceptarEntradaHuella("26"))
    @Test fun aceptaDecimalConComa() = assertTrue(aceptarEntradaHuella("6,8"))
    @Test fun rechaza27() = assertFalse(aceptarEntradaHuella("27"))
    @Test fun rechaza40() = assertFalse(aceptarEntradaHuella("40"))
}
