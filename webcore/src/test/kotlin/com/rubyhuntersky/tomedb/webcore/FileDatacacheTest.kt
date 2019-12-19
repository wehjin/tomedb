package com.rubyhuntersky.tomedb.webcore

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.FileDatalog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FileDatacacheTest {

    private val attr1 = Keyword("Group1", "field1")
    private val attr2 = Keyword("Group2", "field2")

    private val datalog = FileDatalog(
        rootDir = createTempDir("main", "fileDatacacheTest").also { println("Data location: $it") }
    ).apply {
        append(1000, attr1, "a")
        commit()
    }

    @Test
    fun main() {
        val cacheDir =
            createTempDir("main", "fileDatacacheTest").also { println("Data location: $it") }
        val datacache = FileDatacache(cacheDir, datalog)

        val height = datalog.height
        datacache.liftEntToHeight(1000, height)

        val datalist1 = datacache.toDatalist()
        assertEquals(setOf(attr1), datalist1.attrs(1000).toSet())

        datalog.append(1000, attr2, "b")
        val datalist2 = datacache.toDatalist()
        assertEquals(setOf(attr1, attr2), datalist2.attrs(1000).toSet())
        assertEquals(setOf(attr1), datalist1.attrs(1000).toSet())
    }
}