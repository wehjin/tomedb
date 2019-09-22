package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.File

class FileDatalogTest2 {

    enum class Counter : Attribute {
        COUNT {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.ONE
            override val description = "The count associated with a counter."
        },

        MAXCOUNT {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.ONE
            override val description = "The maximum value of the counter."
        },

        COUNTSET {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.MANY
            override val description = "A count that accumulates values."
        };

        override fun toString(): String = attrName.toString()
    }

    private lateinit var folderPath: File


    @Before
    fun setUp() {
        folderPath = TempDirFixture.initDir("fileDatalogTest2").toFile()
    }

    @Test
    fun assertedValuesPersist() {
        val datalog = FileDatalog(folderPath)
        datalog.append(1, Counter.COUNT.attrName, 3)
        datalog.commit()
        assertEquals(3, (datalog.values().first() as Long))

        val datalog2 = FileDatalog(folderPath)
        assertEquals(3, (datalog2.values().first() as Long))
    }

    @Test
    fun neverAssertedValuesReturnFalseForIsAsserted() {
        val datalog = FileDatalog(folderPath)
        assertFalse(datalog.isAsserted(3, Counter.MAXCOUNT.attrName, "Hello"))
    }

    @Test
    fun findMultipleValuesAfterAsserting() {
        val datalog = FileDatalog(folderPath)
        Counter.COUNTSET.toSchemeData().forEach { (value, keyword) ->
            datalog.append(2000, keyword, value)
        }
        datalog.append(1, Counter.COUNTSET.attrName, 3)
        datalog.append(1, Counter.COUNTSET.attrName, 4)
        assertEquals(2, datalog.values(1, Counter.COUNTSET.attrName).toList().size)
    }
}