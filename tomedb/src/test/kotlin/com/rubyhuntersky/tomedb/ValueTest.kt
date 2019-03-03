package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class ValueTest {

    @Test
    fun ref() {
        assertEquals(Ref(), Value.REF(Ref()).v)
    }

    @Test
    fun symbol() {
        assertEquals(AttrName("a", "b"), Value.ATTRNAME(AttrName("a", "b")).v)
    }

    @Test
    fun date() {
        assertEquals(Date(123456), Value.DATE(Date(123456)).v)
    }

    @Test
    fun boolean() {
        assertEquals(true, Value.BOOLEAN(true).v)
    }

    @Test
    fun string() {
        assertEquals("test", Value.STRING("test").v)
    }

    @Test
    fun long() {
        assertEquals(1L, Value.LONG(1L).v)
    }

    @Test
    fun double() {
        assertEquals(3.1, Value.DOUBLE(3.1).v, 0.0000000001)
    }

    @Test
    fun bigDecimal() {
        assertEquals(BigDecimal("123.456"), Value.BIGDEC(BigDecimal("123.456")).v)
    }

    @Test
    fun value() {
        assertEquals(Value.LONG(1L), Value.VALUE(Value.LONG(1L)).v)
    }
}