package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Cardinality
import com.rubyhuntersky.tomedb.basics.Value.LONG
import com.rubyhuntersky.tomedb.basics.ValueType
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Path

class GitDatalogTest {

    private val timeClock = TimeClockFixture()

    enum class Counter : Attribute {
        COUNT {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The count associated with a counter."
        }
    }

    private lateinit var folderPath: Path

    @Before
    fun setUp() {
        val tempFolder = System.getProperty("java.io.tmpdir")
        val testFolder = File(tempFolder, "gitDatalogTest").also {
            it.deleteRecursively()
            println("FOLDER:: ${it.absolutePath}")
        }
        folderPath = testFolder.toPath()
    }

    @Test
    fun main() {
        val datalog = GitDatalog(timeClock, folderPath)
        datalog.append(1, Counter.COUNT.itemName, LONG.of(3))
        assertEquals(3, (datalog.allValues.first() as LONG).v)

        val datalog2 = GitDatalog(timeClock, folderPath)
        assertEquals(3, (datalog2.allValues.first() as LONG).v)
    }
}