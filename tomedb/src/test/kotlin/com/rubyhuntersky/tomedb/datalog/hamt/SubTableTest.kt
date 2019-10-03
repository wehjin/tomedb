package com.rubyhuntersky.tomedb.datalog.hamt

import org.junit.Assert.assertEquals
import org.junit.Test

class SubTableTest {

    @Test(expected = IllegalArgumentException::class)
    fun negativeKey() {
        SubTable.new().postValue(-1, 24L)
    }

    @Test
    fun maxKey() {
        val subTable = SubTable.new().postValue(Long.MAX_VALUE, 24L)
        val value = subTable.getValue(Long.MAX_VALUE)
        assertEquals(24L, value)
    }

    @Test
    fun dissimilarKeysExceedingSubTableSize() {
        val testCount = SubTable.slotCount * SubTable.slotCount
        val tests = (0L until testCount).associateWith { key -> 2 * key }
        val final = tests.entries.fold(
            initial = SubTable.new(),
            operation = { subTable, (key, value) ->
                subTable.postValue(key, value)
            }
        )
        tests.forEach { (key, expected) ->
            assertEquals("key $key", expected, final.getValue(key))
        }
    }

    @Test
    fun twoKeysSameIndex() {
        val keyBreaker = object : KeyBreaker {
            override fun slotIndex(key: Long, depth: Int): Int {
                return if (depth < 5) depth else (key.toInt() and 0b11111)
            }
        }
        val tests = mapOf(1L to 1L, 2L to 2L)
        val final = tests.entries.fold(
            initial = SubTable.new(),
            operation = { subTable, (key, value) ->
                subTable.postValue(keyBreaker, key, value)
            }
        )
        tests.forEach { (key, expected) ->
            val value = final.getValue(keyBreaker, key)
            assertEquals("key $key", expected, value)
        }
    }
}