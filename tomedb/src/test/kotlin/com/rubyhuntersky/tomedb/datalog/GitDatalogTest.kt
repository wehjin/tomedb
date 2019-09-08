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

class GitDatalogTest {

    enum class Counter : Attribute {
        COUNT {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The count associated with a counter."
        },

        MAXCOUNT {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The maximum value of the counter."
        },

        COUNTSET {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.MANY
            override val description: String = "A count that accumulates values."
        };

        override fun toString(): String = attrName.toString()
    }

    private lateinit var folderPath: File


    @Before
    fun setUp() {
        folderPath = TempDirFixture.initDir("gitDatalogTest").toFile()
    }

    @Test
    fun assertedValuesPersist() {
        val datalog = GitDatalog(folderPath)
        datalog.append(1, Counter.COUNT.attrName, 3)
        assertEquals(3, (datalog.values().first() as Long))

        val datalog2 = GitDatalog(folderPath)
        assertEquals(3, (datalog2.values().first() as Long))
    }

    @Test
    fun neverAssertedValuesReturnFalseForIsAsserted() {
        val datalog = GitDatalog(folderPath)
        assertFalse(datalog.isAsserted(3, Counter.MAXCOUNT.attrName, "Hello"))
    }

    @Test
    fun findMultipleValuesAfterAsserting() {
        val datalog = GitDatalog(folderPath)
        Counter.COUNTSET.toSchemeData().forEach { (value, keyword) ->
            datalog.append(2000, keyword, value)
        }
        datalog.append(1, Counter.COUNTSET.attrName, 3)
        datalog.append(1, Counter.COUNTSET.attrName, 4)
        assertEquals(2, datalog.values(1, Counter.COUNTSET.attrName).toList().size)
    }
}