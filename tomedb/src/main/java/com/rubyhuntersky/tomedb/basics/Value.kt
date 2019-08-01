package com.rubyhuntersky.tomedb.basics

import java.math.BigDecimal
import java.util.*

sealed class Value(val valueType: ValueType) {

    val typeId: Int
        get() = valueType.typeId

    data class BOOLEAN(val v: Boolean) : Value(ValueType.BOOLEAN)
    data class LONG(val v: Long) : Value(ValueType.LONG)
    data class STRING(val v: String) : Value(ValueType.STRING)
    data class NAME(val v: Meter) : Value(ValueType.NAME)
    data class INSTANT(val v: Date) : Value(ValueType.INSTANT)
    data class DOUBLE(val v: Double) : Value(ValueType.DOUBLE)
    data class BIGDEC(val v: BigDecimal) : Value(ValueType.BIGDEC)
    data class VALUE(val v: Value) : Value(ValueType.VALUE)
    data class DATA(val v: List<Pair<Meter, Value>>) : Value(ValueType.DATA)
}