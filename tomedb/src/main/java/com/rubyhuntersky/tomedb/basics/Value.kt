package com.rubyhuntersky.tomedb.basics

import java.math.BigDecimal
import java.util.*

sealed class Value<T : Any> {

    abstract val valueType: ValueType
    abstract val valueClass: Class<T>
    abstract val v: T

    data class BOOLEAN(override val v: Boolean) : Value<Boolean>() {
        override val valueType = ValueType.BOOLEAN
        override val valueClass = Boolean::class.java
    }

    data class LONG(override val v: Long) : Value<Long>() {
        override val valueType = ValueType.LONG
        override val valueClass = Long::class.java
    }

    data class STRING(override val v: String) : Value<String>() {
        override val valueType = ValueType.STRING
        override val valueClass = String::class.java
    }

    data class ATTR(override val v: Attr) : Value<Attr>() {
        override val valueType = ValueType.ATTR
        override val valueClass = Attr::class.java
    }

    data class INSTANT(override val v: Date) : Value<Date>() {
        override val valueType = ValueType.INSTANT
        override val valueClass = Date::class.java
    }

    data class DOUBLE(override val v: Double) : Value<Double>() {
        override val valueType = ValueType.DOUBLE
        override val valueClass = Double::class.java
    }

    data class BIGDEC(override val v: BigDecimal) : Value<BigDecimal>() {
        override val valueType = ValueType.BIGDEC
        override val valueClass = BigDecimal::class.java
    }

    data class VALUE(override val v: AnyValue) : Value<AnyValue>() {
        override val valueType = ValueType.VALUE
        override val valueClass = AnyValue::class.java
    }

    data class DATA(override val v: TagList) : Value<TagList>() {
        override val valueType = ValueType.DATA
        override val valueClass = TagList::class.java
    }
}
