package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.GroupedItem
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupedItemTest {

    enum class A : GroupedItem {
        U
    }

    enum class B(val typeId: Int) : GroupedItem {
        V(1)
    }

    object C {
        object W : GroupedItem
    }

    interface GroupedItemItem : GroupedItem {
        val hello: Int
    }

    enum class D : GroupedItemItem {
        X {
            override val hello: Int = 3
        }
    }


    @Test
    fun simpleEnum() {
        assertEquals("A/U", A.U.toGroupedItemString())
    }

    @Test
    fun dataEnum() {
        assertEquals("B/V", B.V.toGroupedItemString())
    }

    @Test
    fun objectInObject() {
        assertEquals("C/W", C.W.toGroupedItemString())
    }

    @Test
    fun implementingEnum() {
        assertEquals("D/X", D.X.toGroupedItemString())
    }
}