package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.ValueType
import com.rubyhuntersky.tomedb.basics.b64Encoder

internal fun Value.toFolderName(): String = valueType.typeCode + toUntypedFolderName()

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
        ValueType.DATA -> "d"
    }

private fun Value.toUntypedFolderName(): String = when (this) {
    is Value.BOOLEAN -> (if (v) 1 else 0).toString()
    is Value.NAME -> {
        val first = b64Encoder.encodeToString(v.first.toByteArray())
        val last = b64Encoder.encodeToString(v.last.toByteArray())
        "$first,$last"
    }
    is Value.INSTANT -> v.time.toString()
    is Value.STRING -> b64Encoder.encodeToString(v.toByteArray())
    is Value.LONG -> v.toString()
    is Value.DOUBLE -> v.toString()
    is Value.BIGDEC -> v.toString()
    is Value.VALUE -> v.toFolderName()
    is Value.DATA -> error("Not supported for data values.")
}
