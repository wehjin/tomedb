package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.GroupedItem
import com.rubyhuntersky.tomedb.attributes.fallbackGroupName
import com.rubyhuntersky.tomedb.attributes.fallbackItemName
import com.rubyhuntersky.tomedb.attributes.toGroupedItemString
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupedItemTest {

    enum class A : GroupedItem {
        U;

        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
    }

    enum class B : GroupedItem {
        V;

        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
    }

    object C {
        object W : GroupedItem {
            override val itemName: String get() = fallbackItemName
            override val groupName: String get() = fallbackGroupName
        }
    }

    interface GroupedItemItem : GroupedItem {
        val hello: Int
        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
    }

    enum class D : GroupedItemItem {
        X {
            override val hello: Int = 3
        };
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