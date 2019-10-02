package com.rubyhuntersky.tomedb.datalog.hamt

import org.junit.Assert.assertEquals
import org.junit.Test

class SubTableTest {

    @Test
    fun newTablePostValue() {
        val subTable = SubTable.new()
        val key = 42L
        val newSubTable = subTable.postValue(Hamt.toIndices(key), key, 24L)
        assertEquals(24L, newSubTable.getValue(Hamt.toIndices(key), key))
    }
}