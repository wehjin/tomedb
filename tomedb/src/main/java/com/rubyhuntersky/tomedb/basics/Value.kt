package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.Attribute
import java.math.BigDecimal
import java.util.*

sealed class Value(val valueType: ValueType) {

    data class REF(val v: Ref) : Value(ValueType.REF)
    data class NAME(val v: ItemName) : Value(ValueType.TAG)
    data class INSTANT(val v: Date) : Value(ValueType.DATE)
    data class BOOLEAN(val v: Boolean) : Value(ValueType.BOOLEAN)
    data class STRING(val v: String) : Value(ValueType.STRING)
    data class LONG(val v: Long) : Value(ValueType.LONG)
    data class DOUBLE(val v: Double) : Value(ValueType.DOUBLE)
    data class BIGDEC(val v: BigDecimal) : Value(ValueType.BIGDEC)
    data class VALUE(val v: Value) : Value(ValueType.VALUE)
    data class DATA(val v: List<Pair<Attribute, Value>>) : Value(ValueType.DATA)
}