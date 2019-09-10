package com.rubyhuntersky.tomedb.datalog.hamt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UniHashTest {

    @Test
    fun sameTwice() {
        val once = UniHash.hashLong(0x20001)
        val again = UniHash.hashLong(0x20001)
        assertEquals(once, again)
    }

    @Test
    fun sequentialDifferent() {
        val first = UniHash.hashLong(0x20001)
        val second = UniHash.hashLong(0x10001)
        assertNotEquals(first, second)
    }

    @Test
    fun negativeDifferent() {
        val one = UniHash.hashLong(1)
        val negOne = UniHash.hashLong(-1)
        val negTwo = UniHash.hashLong(-2)
        assertNotEquals(one, negOne)
        assertNotEquals(one, negTwo)
    }
}