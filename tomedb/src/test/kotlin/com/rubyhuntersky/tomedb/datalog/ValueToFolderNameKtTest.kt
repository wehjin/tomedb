package com.rubyhuntersky.tomedb.datalog

import org.junit.Assert.assertEquals
import org.junit.Test

class ValueToFolderNameKtTest {

    @Test
    fun string() {
        val value = "Return of the King"
        val folderName = value.toFolderName()
        val value2 = valueOfFolderName(folderName)
        assertEquals(value, value2)
    }
}

