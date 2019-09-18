package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.basics.Keyword
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        assertEquals(entity, datalog.ents().single())
    }
}