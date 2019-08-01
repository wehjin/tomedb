package com.rubyhuntersky.tomedb.basics

import org.junit.Assert.assertEquals
import org.junit.Test

class NamedItemTest {

    enum class A : Meter {
        U
    }

    enum class B(val typeId: Int) : Meter {
        V(1)
    }

    object C {
        object W : Meter
    }

    interface NamedItemItem : Meter {
        val hello: Int
    }

    enum class D : NamedItemItem {
        X {
            override val hello: Int = 3
        }
    }


    @Test
    fun simpleEnum() {
        assertEquals("A/U", A.U.toMeterString())
    }

    @Test
    fun dataEnum() {
        assertEquals("B/V", B.V.toMeterString())
    }

    @Test
    fun objectInObject() {
        assertEquals("C/W", C.W.toMeterString())
    }

    @Test
    fun implementingEnum() {
        assertEquals("D/X", D.X.toMeterString())
    }
}