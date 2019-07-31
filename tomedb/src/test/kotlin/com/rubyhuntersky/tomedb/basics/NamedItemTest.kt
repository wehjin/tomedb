package com.rubyhuntersky.tomedb.basics

import junit.framework.Assert.assertEquals
import org.junit.Test

class NamedItemTest {

    enum class A : NamedItem {
        U
    }

    enum class B(val typeId: Int) : NamedItem {
        V(1)
    }

    object C {
        object W : NamedItem
    }

    interface NamedItemItem : NamedItem {
        val hello: Int
    }

    enum class D : NamedItemItem {
        X {
            override val hello: Int = 3
        }
    }


    @Test
    fun simpleEnum() {
        assertEquals("A/U", A.U.itemName.toString())
    }

    @Test
    fun dataEnum() {
        assertEquals("B/V", B.V.itemName.toString())
    }

    @Test
    fun objectInObject() {
        assertEquals("C/W", C.W.itemName.toString())
    }

    @Test
    fun implementingEnum() {
        assertEquals("D/X", D.X.itemName.toString())
    }
}