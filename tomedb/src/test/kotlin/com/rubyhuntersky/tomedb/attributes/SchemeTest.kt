package com.rubyhuntersky.tomedb.attributes

import org.junit.Assert.assertEquals
import org.junit.Test

class SchemeTest {
    @Test
    fun attrString() {
        val attr = Scheme.NAME
        assertEquals("Scheme/NAME", attr.toString())
    }
}