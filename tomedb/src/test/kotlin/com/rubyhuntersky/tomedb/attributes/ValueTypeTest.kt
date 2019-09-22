package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword
import org.junit.Assert.assertEquals
import org.junit.Test

class ValueTypeTest {

    @Test
    fun keyword() {
        val valueType = ValueType.BOOLEAN
        assertEquals(Keyword("BOOLEAN", "ValueType"), valueType.keyword)
    }
}