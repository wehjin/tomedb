package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.ValueType
import java.math.BigDecimal
import java.util.*

sealed class Value {
    abstract val valueType: ValueType

    data class REF(val v: Ref) : Value() {
        override val valueType: ValueType
            get() = ValueType.REF
    }

    data class TAG(val v: NamedItem) : Value() {
        override val valueType: ValueType
            get() = ValueType.TAG
    }

    data class DATE(val v: Date) : Value() {
        override val valueType: ValueType
            get() = ValueType.DATE
    }

    data class BOOLEAN(val v: Boolean) : Value() {
        override val valueType: ValueType
            get() = ValueType.BOOLEAN
    }

    data class STRING(val v: String) : Value() {
        override val valueType: ValueType
            get() = ValueType.STRING
    }

    data class LONG(val v: Long) : Value() {
        override val valueType: ValueType
            get() = ValueType.LONG
    }

    data class DOUBLE(val v: Double) : Value() {
        override val valueType: ValueType
            get() = ValueType.DOUBLE
    }

    data class BIGDEC(val v: BigDecimal) : Value() {
        override val valueType: ValueType
            get() = ValueType.BIGDEC
    }

    data class VALUE(val v: Value) : Value() {
        override val valueType: ValueType
            get() = ValueType.VALUE
    }

    data class DATA(val v: List<Pair<Attribute, Value>>) : Value() {
        override val valueType: ValueType
            get() = ValueType.DATA
    }
}