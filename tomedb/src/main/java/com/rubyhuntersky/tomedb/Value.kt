package com.rubyhuntersky.tomedb

import java.math.BigDecimal
import java.util.*

data class AttrName(val first: String, val last: String)

sealed class Value {
    abstract val valueType: ValueType

    data class REF(val ref: Ref) : Value() {
        override val valueType: ValueType
            get() = ValueType.REF
    }

    data class ATTRNAME(val attrName: AttrName) : Value() {
        override val valueType: ValueType
            get() = ValueType.ATTRNAME
    }

    data class DATE(val date: Date) : Value() {
        override val valueType: ValueType
            get() = ValueType.DATE
    }

    data class BOOLEAN(val boolean: Boolean) : Value() {
        override val valueType: ValueType
            get() = ValueType.BOOLEAN
    }

    data class STRING(val string: String) : Value() {
        override val valueType: ValueType
            get() = ValueType.STRING
    }

    data class LONG(val long: Long) : Value() {
        override val valueType: ValueType
            get() = ValueType.LONG
    }

    data class DOUBLE(val double: Double) : Value() {
        override val valueType: ValueType
            get() = ValueType.DOUBLE
    }

    data class BIGDEC(val bigDecimal: BigDecimal) : Value() {
        override val valueType: ValueType
            get() = ValueType.BIGDEC
    }

    data class VALUE(val value: Value) : Value() {
        override val valueType: ValueType
            get() = ValueType.VALUE
    }

    data class DATA(val data: Map<Enum<*>, Value>) : Value() {
        override val valueType: ValueType
            get() = ValueType.DATA
    }
}