package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.folderNameToString
import com.rubyhuntersky.tomedb.basics.stringToFolderName

object AttrCoder {
    fun folderNameFromAttr(attr: Keyword): String {
        val first = stringToFolderName(attr.keywordName)
        val last = stringToFolderName(attr.keywordGroup)
        return "$first,$last"
    }

    fun attrFromFolderName(string: String): Keyword {
        return string.split(',').let {
            val keywordName = folderNameToString(it[0])
            val keywordGroup = folderNameToString(it[1])
            Keyword(keywordName, keywordGroup)
        }
    }
}