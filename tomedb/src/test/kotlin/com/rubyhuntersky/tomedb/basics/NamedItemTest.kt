package com.rubyhuntersky.tomedb.basics

import org.junit.Assert.assertEquals
import org.junit.Test

class NamedItemTest {

    enum class A : Attr {
        U
    }

    enum class B(val typeId: Int) : Attr {
        V(1)
    }

    object C {
        object W : Attr
    }

    interface NamedItemItem : Attr {
        val hello: Int
    }

    enum class D : NamedItemItem {
        X {
            override val hello: Int = 3
        }
    }


    @Test
    fun simpleEnum() {
        assertEquals("A/U", A.U.toAttrString())
    }

    @Test
    fun dataEnum() {
        assertEquals("B/V", B.V.toAttrString())
    }

    @Test
    fun objectInObject() {
        assertEquals("C/W", C.W.toAttrString())
    }

    @Test
    fun implementingEnum() {
        assertEquals("D/X", D.X.toAttrString())
    }
}