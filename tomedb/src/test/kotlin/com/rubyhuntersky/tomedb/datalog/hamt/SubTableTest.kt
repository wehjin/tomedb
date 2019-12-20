package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.TempDirFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

class SubTableTest {

    @Test
    fun harden() {
        val saveFile = createSaveFile("harden")
        val io = SubTableIo(saveFile)

        // Creating a table, modifying it, and hardening it should produce a hard table.
        val tests = (0 until 50L).map { Pair(it, it) }
        val softTable = SubTable.new().setValues(tests)
        val hardTable = softTable.harden(io)
        SubTable.load(hardTable.slotMap, hardTable.base, subTableReader(saveFile))
            .assertKeyValues(tests)

        // Loading a table, modifying it, and hardening it should produce a different hard table.
        val expansion = (51L until 100L).map { Pair(it, it) }
        val expansionTable =
            SubTable.load(hardTable.slotMap, hardTable.base, subTableReader(saveFile))
                .setValues(expansion)
        val hardTable2 = expansionTable.harden(io)
        SubTable.load(hardTable2.slotMap, hardTable2.base, subTableReader(saveFile))
            .assertKeyValues(tests + expansion)

        // The first hard table should remain viable.
        SubTable.load(hardTable.slotMap, hardTable.base, subTableReader(saveFile)).apply {
            assertKeyValues(tests)
            assertNoKeyValues(expansion)
        }
    }

    private fun SubTable.assertKeyValues(tests: List<Pair<Long, Long>>) {
        tests.forEach { (key, expectedValue) ->
            val value = getValue(key)
            assertEquals("key: $key", expectedValue, value)
        }
    }

    private fun SubTable.assertNoKeyValues(tests: List<Pair<Long, Long>>) {
        tests.forEach { (key, _) ->
            val value = getValue(key)
            assertNull("key: $key", value)
        }
    }

    private fun createSaveFile(name: String): File {
        return TempDirFixture.initDir(name).toFile()
            .also { it.mkdirs() }
            .let { dir -> File(dir, "save").also { it.delete(); it.createNewFile() } }
    }

    @Test
    fun dissimilarKeysExceedingSubTableSize() {
        val testCount = SubTable.slotCount * SubTable.slotCount
        val tests = (0L until testCount).associateWith { key -> 2 * key }
        val final = tests.entries.fold(
            initial = SubTable.new(),
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
            initial = SubTable.new(),
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