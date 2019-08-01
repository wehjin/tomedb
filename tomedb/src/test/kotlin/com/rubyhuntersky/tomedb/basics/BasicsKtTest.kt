package com.rubyhuntersky.tomedb.basics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BasicsKtTest {

    @Test
    fun stringToFolderNameRemovesSlashes() {
        val folderName = stringToFolderName("Hello?")
        assertFalse(folderName.contains("/"))
        assertEquals("Hello?", folderNameToString(folderName))
    }
}