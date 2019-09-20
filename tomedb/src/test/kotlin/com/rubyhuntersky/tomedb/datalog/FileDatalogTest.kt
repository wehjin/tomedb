package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.basics.Keyword
import org.junit.Assert.*
import org.junit.Test

class FileDatalogTest {

    private val tempDir = TempDirFixture.initDir("fileDatalogTest").toFile()
    private val datalog = FileDatalog(tempDir)
    private val entity: Long = 17
    private val attr = Keyword("a", "b")
    private val value = "Hello"

    @Test
    fun newIsEmpty() {
        assertTrue(datalog.ents().toSet().isEmpty())
    }

    @Test
    fun append() {
        val fact = datalog.append(entity, attr, value)
        assertEquals(entity, fact.entity)
        assertEquals(attr, fact.attr)
        assertEquals(value, fact.value)
        assertEquals(setOf(entity), datalog.ents(attr).toSet())
        assertEquals(setOf(entity), datalog.ents().toSet())
        assertEquals(setOf(attr), datalog.attrs(entity).toSet())
        assertEquals(setOf(attr), datalog.attrs().toSet())
        assertEquals(setOf("Hello"), datalog.values(entity, attr).toSet())
        assertEquals(setOf("Hello"), datalog.values().toSet())
        assertTrue(datalog.isAsserted(entity, attr))
        assertTrue(datalog.isAsserted(entity, attr, "Hello"))
        assertFalse(datalog.isAsserted(entity, attr, "Goodbye"))
    }

    @Test
    fun appendRetract() {
        datalog.append(entity, attr, value)
        datalog.append(entity, attr, value, Fact.Standing.Retracted)
        assertEquals(emptySet<Any>(), datalog.values().toSet())
    }
}