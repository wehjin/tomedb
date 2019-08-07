package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LineTest {

    @Test
    fun isComposedOfKeywordAndValue() {
        val line = lineOf(Citizen.FullName.attrName, "Miranda")
        assertEquals(Citizen.FullName.attrName, line.attr)
        assertEquals("Miranda", line.value)
    }
}