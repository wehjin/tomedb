package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.folderNameToString
import com.rubyhuntersky.tomedb.basics.stringToFolderName
import java.math.BigDecimal
import java.util.*

internal fun <T : Any> T.toFolderName(): String {
    return typeCodeOfValue(this) + toFolderNameUntyped()
}

private fun <T : Any> T.toFolderNameUntyped(): String = when (this) {
    is Boolean -> (if (this) 1 else 0).toString()
    is Long -> this.toString()
    is Int -> this.toString()
    is String -> stringToFolderName(this)
    is Keyword -> {
        val first = stringToFolderName(this.keywordName)
        val last = stringToFolderName(this.keywordGroup)
        "$first,$last"
    }
    is Date -> this.time.toString()
    is Double -> this.toString()
    is BigDecimal -> this.toString()
    else -> "Not supported for value of type: ${this::class.java.simpleName}"
}

internal fun valueOfFolderName(folderName: String): Any {
    val typeCode = folderName.substring(0, 1)
    val content = folderName.substring(1)
    return when (typeCode) {
        BooleanCode -> toBooleanValue(content)
        LongCode -> toLongValue(content)
        StringCode -> toStringValue(content)
        KeywordCode -> toKeywordValue(content)
        DateCode -> toDateValue(content)
        DoubleCode -> toDoubleValue(content)
        BigDecimalCode -> toBigDecimalValue(content)
        TagListCode -> toTagListValue()
        else -> error("Unknown type code in folder name: $folderName")
    }
}

private fun toBooleanValue(content: String): Boolean = (content == "1")
private fun toLongValue(content: String): Long = content.toLong()
private fun toStringValue(content: String): String = folderNameToString(content)
private fun toKeywordValue(content: String): Keyword {
    val (first, last) = content.split(',')
    return Keyword(folderNameToString(first), folderNameToString(last))
}

private fun toDateValue(content: String): Date = Date(content.toLong())
private fun toDoubleValue(content: String): Double = content.toDouble()
private fun toBigDecimalValue(content: String): BigDecimal = content.toBigDecimal()
private fun toTagListValue(): TagList = error("Not Supported")

private fun typeCodeOfValue(v: Any): String = when (v) {
    is Boolean -> BooleanCode
    is Long -> LongCode
    is Int -> LongCode
    is String -> StringCode
    is Keyword -> KeywordCode
    is Date -> DateCode
    is Double -> DoubleCode
    is BigDecimal -> BigDecimalCode
    is TagList -> TagListCode
    else -> error("Unsupported type: ${v::class.java.simpleName}.")
}

private const val BooleanCode = "b"
private const val LongCode = "l"
private const val StringCode = "s"
private const val KeywordCode = "a"
private const val DateCode = "i"
private const val DoubleCode = "d"
private const val BigDecimalCode = "t"
private const val TagListCode = "z"
