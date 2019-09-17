package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.TempDirFixture
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FileDatalogTest {

    @Before
    fun setUp() {
        tempDir = TempDirFixture.initDir("fileDatalogTest").toFile()
    }

    private lateinit var tempDir: File

    @Test
    fun newIsEmpty() {
        val datalog = FileDatalog(tempDir)
        assertTrue(datalog.ents().toSet().isEmpty())
    }
}