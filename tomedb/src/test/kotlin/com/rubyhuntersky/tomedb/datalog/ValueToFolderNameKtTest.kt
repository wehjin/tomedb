package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Value
import org.junit.Assert.assertEquals
import org.junit.Test

class ValueToFolderNameKtTest {

    @Test
    fun string() {
        val value = Value.STRING.of("Return of the King")
        val folderName = value.toFolderName()
        val value2 = valueOfFolderName(folderName)
        assertEquals(value, value2)
    }
}

