package com.rubyhuntersky.tomedb.basics

import org.junit.Assert.assertEquals
import org.junit.Test

class NamedItemTest {

    enum class A : Keyword {
        U
    }

    enum class B(val typeId: Int) : Keyword {
        V(1)
    }

    object C {
        object W : Keyword
    }

    interface NamedItemItem : Keyword {
        val hello: Int
    }

    enum class D : NamedItemItem {
        X {
            override val hello: Int = 3
        }
    }


    @Test
    fun simpleEnum() {
        assertEquals("A/U", A.U.toKeywordString())
    }

    @Test
    fun dataEnum() {
        assertEquals("B/V", B.V.toKeywordString())
    }

    @Test
    fun objectInObject() {
        assertEquals("C/W", C.W.toKeywordString())
    }

    @Test
    fun implementingEnum() {
        assertEquals("D/X", D.X.toKeywordString())
    }
}