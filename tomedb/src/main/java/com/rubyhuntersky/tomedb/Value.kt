package com.rubyhuntersky.tomedb

import java.math.BigDecimal
import java.util.*

data class AttrName(val first: String, val last: String) {
    override fun toString(): String = "$first/$last"
}

sealed class Value {
    abstract val valueType: ValueType

    data class REF(val v: Ref) : Value() {
        override val valueType: ValueType
            get() = ValueType.REF
    }

    data class ATTRNAME(val v: AttrName) : Value() {
        override val valueType: ValueType
            get() = ValueType.ATTRNAME
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

    data class DATA(val v: List<Pair<Enum<*>, Value>>) : Value() {
        override val valueType: ValueType
            get() = ValueType.DATA
    }
}