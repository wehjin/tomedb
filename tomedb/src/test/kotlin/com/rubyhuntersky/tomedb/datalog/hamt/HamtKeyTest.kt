package com.rubyhuntersky.tomedb.datalog.hamt

import org.junit.Assert.assertEquals
import org.junit.Test

class HamtKeyTest {

    @Test
    fun indices() {
        val hash: Long = 0x7000030000210001
        val key = HamtKey(hash)
        val toIndices = key.toIndices()
        val indices = toIndices.toList()
        assertEquals(listOf<Byte>(1, 0, 0, 2, 2, 0, 0, 0, 3, 0, 0, 0, 7), indices)
    }

    @Test
    fun negative() {
        val hash: Long = -1
        val key = HamtKey(hash)
        val toIndices = key.toIndices()
        val indices = toIndices.toList()
        assertEquals((0..60 step 5).map { 31.toByte() }, indices)
    }
}