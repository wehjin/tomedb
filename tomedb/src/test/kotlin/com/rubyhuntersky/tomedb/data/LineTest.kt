package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LineTest {

    @Test
    fun isComposedOfKeywordAndValue() {
        val line = lineOf(Citizen.FullName, "Miranda")
        assertEquals(Citizen.FullName.attrName, line.lineAttr)
        assertEquals("Miranda", line.lineValue)
    }
}