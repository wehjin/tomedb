package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.TempDirFixture
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

class SubTableTest {

    @Test
    fun harden() {
        val file = TempDirFixture.initDir("harden").toFile()
            .also { it.mkdirs() }
            .let { dir -> File(dir, "save").also { it.delete(); it.createNewFile() } }

        val softTable = SubTable.new().setValue(1L, 1L)
        val readWrite = SubTableReadWrite(file)
        val hardTable = softTable.harden(readWrite)
        val value = hardTable.getValue(1L)
        assertEquals(1L, value)
    }

    @Test
    fun dissimilarKeysExceedingSubTableSize() {
        val testCount = SubTable.slotCount * SubTable.slotCount
        val tests = (0L until testCount).associateWith { key -> 2 * key }
        val final = tests.entries.fold(
            initial = SubTable.new() as SubTable,
            operation = { subTable, (key, value) ->
                subTable.setValue(key, value)
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
            initial = SubTable.new() as SubTable,
            operation = { subTable, (key, value) ->
                subTable.setValue(keyBreaker, key, value)
            }
        )
        tests.forEach { (key, expected) ->
            val value = final.getValue(keyBreaker, key)
            assertEquals("key $key", expected, value)
        }
    }

    @Test
    fun rewriteValueSameKey() {
        val key: Long = Random.nextLong().absoluteValue
        val subTable = SubTable.new().setValue(key, 24L).setValue(key, 48L)
        val value = subTable.getValue(key)
        assertEquals(48L, value)

    }

    @Test
    fun maxKey() {
        val subTable = SubTable.new().setValue(Long.MAX_VALUE, 24L)
        val value = subTable.getValue(Long.MAX_VALUE)
        assertEquals(24L, value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeKey() {
        SubTable.new().setValue(-1, 24L)
    }
}