package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class ValueTest {

    @Test
    fun ref() {
        assertEquals(Ref(), Value.REF(Ref()).ref)
    }

    @Test
    fun symbol() {
        assertEquals(AttrName("a", "b"), Value.ATTRNAME(AttrName("a", "b")).attrName)
    }

    @Test
    fun date() {
        assertEquals(Date(123456), Value.DATE(Date(123456)).date)
    }

    @Test
    fun boolean() {
        assertEquals(true, Value.BOOLEAN(true).boolean)
    }

    @Test
    fun string() {
        assertEquals("test", Value.STRING("test").string)
    }

    @Test
    fun long() {
        assertEquals(1L, Value.LONG(1L).long)
    }

    @Test
    fun double() {
        assertEquals(3.1, Value.DOUBLE(3.1).double, 0.0000000001)
    }

    @Test
    fun bigDecimal() {
        assertEquals(BigDecimal("123.456"), Value.BIGDEC(BigDecimal("123.456")).bigDecimal)
    }

    @Test
    fun value() {
        assertEquals(Value.LONG(1L), Value.VALUE(Value.LONG(1L)).value)
    }
}