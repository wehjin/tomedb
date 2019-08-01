package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.MeterSpec
import java.math.BigDecimal
import java.util.*

sealed class Value(val valueType: ValueType) {

    val typeId: Int
        get() = valueType.typeId

    data class BOOLEAN(val v: Boolean) : Value(ValueType.BOOLEAN) {
        companion object {
            fun of(boolean: Boolean) = BOOLEAN(boolean)
        }
    }

    data class LONG(val v: Long) : Value(ValueType.LONG) {
        companion object {
            fun of(long: Long) = LONG(long)
        }
    }

    data class STRING(val v: String) : Value(ValueType.STRING) {
        companion object {
            fun of(string: String) = STRING(string)
        }

        override fun toString(): String = v
    }

    data class NAME(val v: Meter) : Value(ValueType.NAME) {
        companion object {
            fun of(itemName: Meter) = NAME(itemName)
        }
    }

    data class INSTANT(val v: Date) : Value(ValueType.INSTANT) {
        companion object {
            fun of(date: Date) = INSTANT(date)
        }
    }

    data class DOUBLE(val v: Double) : Value(ValueType.DOUBLE) {
        companion object {
            fun of(double: Double) = DOUBLE(double)
        }
    }

    data class BIGDEC(val v: BigDecimal) : Value(ValueType.BIGDEC) {
        companion object {
            fun of(bigDecimal: BigDecimal) = BIGDEC(bigDecimal)
        }
    }

    data class VALUE(val v: Value) : Value(ValueType.VALUE) {
        companion object {
            fun of(value: Value) = VALUE(value)
        }
    }

    data class DATA(val v: List<Pair<MeterSpec, Value>>) : Value(ValueType.DATA)
}