package com.rubyhuntersky.tomedb.basics

import java.math.BigDecimal
import java.util.*

sealed class Value(val valueType: ValueType, val valueClass: Class<*>) {

    val typeId: Int
        get() = valueType.typeId

    data class BOOLEAN(val v: Boolean) : Value(ValueType.BOOLEAN, Boolean::class.java)
    data class LONG(val v: Long) : Value(ValueType.LONG, Long::class.java)
    data class STRING(val v: String) : Value(ValueType.STRING, String::class.java)
    data class ATTR(val v: Attr) : Value(ValueType.ATTR, Attr::class.java)
    data class INSTANT(val v: Date) : Value(ValueType.INSTANT, Date::class.java)
    data class DOUBLE(val v: Double) : Value(ValueType.DOUBLE, Double::class.java)
    data class BIGDEC(val v: BigDecimal) : Value(ValueType.BIGDEC, BigDecimal::class.java)
    data class VALUE(val v: Value) : Value(ValueType.VALUE, Any::class.java)
    data class DATA(val v: TagList) : Value(ValueType.DATA, TagList::class.java)
}

