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
        append(1000L, attr1, "x")
        append(1001L, attr2, "x")
    }

    private fun dataCache(name: String): FileDatacache {
        val dir = createTempDir(name, "fileDatacacheTest").also { println("Data location: $it") }
        return FileDatacache(dir, datalog)
    }

    @Test
    fun readAttributesOfEnt() {
        val datacache = dataCache("readAttributesOfEnt")
        val datalist1 = datacache.toDatalist()
        assertEquals(setOf(attr1), datalist1.attrs(1000L).toSet())

        datalog.append(1000L, attr2, "x")
        val datalist2 = datacache.toDatalist()
        assertEquals(setOf(attr1, attr2), datalist2.attrs(1000L).toSet())
        assertEquals(setOf(attr1), datalist1.attrs(1000L).toSet())
    }

    @Test
    fun readEntsOfAttribute() {
        val datacache = dataCache("readEntsOfAttribute")
        val datalist1 = datacache.toDatalist()
        assertEquals(setOf(1000L), datalist1.ents(attr1).toSet())

        datalog.append(1001L, attr1, "x")
        val datalist2 = datacache.toDatalist()
        assertEquals(setOf(1000L, 1001L), datalist2.ents(attr1).toSet())
        assertEquals(setOf(1000L), datalist1.ents(attr1).toSet())
    }
}