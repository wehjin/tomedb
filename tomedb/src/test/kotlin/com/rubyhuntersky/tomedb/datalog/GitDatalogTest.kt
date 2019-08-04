package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.basics.Value.LONG
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.invoke
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
        };

        override fun toString(): String = toKeywordString()
    }

    private lateinit var folderPath: File


    @Before
    fun setUp() {
        folderPath = TempDirFixture.initDir("gitDatalogTest").toFile()
    }

    @Test
    fun assertedValuesPersist() {
        val datalog = GitDatalog(folderPath)
        datalog.append(1, Counter.COUNT, 3())
        assertEquals(3, (datalog.allAssertedValues.first() as LONG).v)

        val datalog2 = GitDatalog(folderPath)
        assertEquals(3, (datalog2.allAssertedValues.first() as LONG).v)
    }

    @Test
    fun neverAssertedValuesReturnFalseForIsAsserted() {
        val datalog = GitDatalog(folderPath)
        assertFalse(datalog.isEntityAttrValueAsserted(3, Counter.MAXCOUNT, "Hello"()))
    }

    @Test
    fun findMultipleValuesAfterAsserting() {
        val datalog = GitDatalog(folderPath)
        datalog.append(1, Counter.COUNT, 3())
        datalog.append(1, Counter.COUNT, 4())
        assertEquals(2, datalog.entityAttrValues(1, Counter.COUNT).size)
    }
}