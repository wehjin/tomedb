package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.basics.Keyword
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileDatalogTest {

    @Test
    fun newIsEmpty() {
        val tempDir = TempDirFixture.initDir("fileDatalogNewIsEmptyTest").toFile()
        val datalog = FileDatalog(tempDir)
        assertTrue(datalog.ents().toSet().isEmpty())
    }

    @Test
    fun append() {
        val tempDir = TempDirFixture.initDir("fileDatalogAppendTest").toFile()
        val datalog = FileDatalog(tempDir)
        val fact = datalog.append(1, Keyword("a", "b"), "Hello")
        assertEquals(1, fact.entity)
        assertEquals(Keyword("a", "b"), fact.attr)
        assertEquals("Hello", fact.value)
        assertEquals(datalog.ents().count(), 1)
    }
}