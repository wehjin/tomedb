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
    is Value.ATTR -> {
        val first = stringToFolderName(v.attrName)
        val last = stringToFolderName(v.attrGroup)
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
        ValueType.BOOLEAN -> (content == "1")()
        ValueType.LONG -> (content.toLong())()
        ValueType.STRING -> {
            folderNameToString(content)()
        }
        ValueType.ATTR -> {
            val (first, last) = content.split(',')
            val itemName = BasicAttr(folderNameToString(first), folderNameToString(last))
            itemName()
        }
        ValueType.INSTANT -> Date(content.toLong())()
        ValueType.DOUBLE -> (content.toDouble())()
        ValueType.BIGDEC -> (content.toBigDecimal())()
        ValueType.VALUE -> valueOfFolderName(content)()
        ValueType.DATA -> error("Not supported")
    }
}

private val ValueType.typeCode: String
    get() = when (this) {
        ValueType.BOOLEAN -> "b"
        ValueType.LONG -> "l"
        ValueType.STRING -> "s"
        ValueType.ATTR -> "a"
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
    "a" -> ValueType.ATTR
    "i" -> ValueType.INSTANT
    "d" -> ValueType.DOUBLE
    "t" -> ValueType.BIGDEC
    "v" -> ValueType.VALUE
    "z" -> ValueType.DATA
    else -> error("Invalid type code: $typeCode")
}
