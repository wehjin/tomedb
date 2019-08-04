package com.rubyhuntersky.tomedb.basics

import java.math.BigDecimal
import java.util.*

sealed class Value<out T : Any>(val valueType: ValueType) {

    val valueClass: Class<out T>
        get() = valueType.toValueClass()

    abstract val v: T

    data class BOOLEAN(override val v: Boolean) : Value<Boolean>(ValueType.BOOLEAN)
    data class LONG(override val v: Long) : Value<Long>(ValueType.LONG)
    data class STRING(override val v: String) : Value<String>(ValueType.STRING)
    data class ATTR(override val v: Keyword) : Value<Keyword>(ValueType.ATTR)
    data class INSTANT(override val v: Date) : Value<Date>(ValueType.INSTANT)
    data class DOUBLE(override val v: Double) : Value<Double>(ValueType.DOUBLE)
    data class BIGDEC(override val v: BigDecimal) : Value<BigDecimal>(ValueType.BIGDEC)
    data class VALUE(override val v: AnyValue) : Value<AnyValue>(ValueType.VALUE)
    data class DATA(override val v: TagList) : Value<TagList>(ValueType.DATA)

    inline fun <reified U : Any> toType(): U = v as U

    companion object {
        fun <T : Any> of(v: T): Value<T> {
            val value = when (v) {
                is Boolean -> BOOLEAN(v)
                is Long -> LONG(v)
                is String -> STRING(v)
                is Keyword -> ATTR(v)
                is Date -> INSTANT(v)
                is Double -> DOUBLE(v)
                is BigDecimal -> BIGDEC(v)
                is AnyValue -> VALUE(v)
                is TagList -> DATA(v)
                else -> error("Invalid value type: $v")
            }
            @Suppress("UNCHECKED_CAST")
            return value as Value<T>
        }
    }
}
