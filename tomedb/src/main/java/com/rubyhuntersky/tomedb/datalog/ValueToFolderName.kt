package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.*
import java.util.*

internal fun Value.toFolderName(): String = valueType.typeCode + toFolderNameUntyped()

internal fun valueOfFolderName(folderName: String): Value {
    val typeCode = folderName.substring(0, 1)
    val content = folderName.substring(1)
    val valueType = valueTypeOfTypeCode(typeCode)
    return valueOfFolderNameWithType(valueType, content)
}

private fun Value.toFolderNameUntyped(): String = when (this) {
    is Value.BOOLEAN -> (if (v) 1 else 0).toString()
    is Value.LONG -> v.toString()
    is Value.STRING -> stringToFolderName(v)
    is Value.NAME -> {
        val first = stringToFolderName(v.first)
        val last = stringToFolderName(v.last)
        "$first,$last"
    }
    is Value.INSTANT -> v.time.toString()
    is Value.DOUBLE -> v.toString()
    is Value.BIGDEC -> v.toString()
    is Value.VALUE -> v.toFolderName()
    is Value.DATA -> error("Not supported.")
}

private fun valueOfFolderNameWithType(valueType: ValueType, content: String): Value {
    return when (valueType) {
        ValueType.BOOLEAN -> Value.BOOLEAN.of(content == "1")
        ValueType.LONG -> Value.LONG.of(content.toLong())
        ValueType.STRING -> {
            Value.STRING.of(folderNameToString(content))
        }
        ValueType.NAME -> {
            val (first, last) = content.split(',')
            val itemName = ItemName(folderNameToString(first), folderNameToString(last))
            Value.NAME.of(itemName)
        }
        ValueType.INSTANT -> Value.INSTANT.of(Date(content.toLong()))
        ValueType.DOUBLE -> Value.DOUBLE.of(content.toDouble())
        ValueType.BIGDEC -> Value.BIGDEC.of(content.toBigDecimal())
        ValueType.VALUE -> Value.VALUE.of(valueOfFolderName(content))
        ValueType.DATA -> error("Not supported")
    }
}

private val ValueType.typeCode: String
    get() = when (this) {
        ValueType.BOOLEAN -> "b"
        ValueType.LONG -> "l"
        ValueType.STRING -> "s"
        ValueType.NAME -> "n"
        ValueType.INSTANT -> "i"
        ValueType.DOUBLE -> "d"
        ValueType.BIGDEC -> "t"
        ValueType.VALUE -> "v"
        ValueType.DATA -> "z"
    }

private fun valueTypeOfTypeCode(typeCode: String): ValueType = when (typeCode) {
    "b" -> ValueType.BOOLEAN
    "l" -> ValueType.LONG
    "s" -> ValueType.STRING
    "n" -> ValueType.NAME
    "i" -> ValueType.INSTANT
    "d" -> ValueType.DOUBLE
    "t" -> ValueType.BIGDEC
    "v" -> ValueType.VALUE
    "z" -> ValueType.DATA
    else -> error("Invalid type code: $typeCode")
}
