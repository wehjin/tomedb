package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.NamedItem
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.ValueType

sealed class Rule {
    data class EExactA(val entityVar: String, val attribute: NamedItem) : Rule()
    data class EExactVA(val entityVar: String, val value: Value, val attribute: NamedItem) : Rule()
    data class EEExactA(val startVar: String, val endVar: String, val attribute: NamedItem) : Rule()
    data class EVExactA(val entityVar: String, val valueVar: String, val attribute: NamedItem) : Rule()
}

data class Input(val label: String, val value: Value) {
    constructor(label: String, long: Long) : this(label, Value.LONG(long))
}

sealed class Query {
    data class Find(val rules: List<Rule>, val inputs: List<Input>? = null, val outputs: List<String>) : Query()
}

sealed class Solutions<T> {
    class None<T> : Solutions<T>() {
        override fun toList(): List<T> = emptyList()
    }

    data class One<T>(val item: T) : Solutions<T>() {
        override fun toList(): List<T> = listOf(item)
    }

    data class Some<T>(val items: List<T>) : Solutions<T>() {
        override fun toList(): List<T> = items
    }

    class Any<T> : Solutions<T>() {
        override fun toList(): List<T> = throw Exception("Solutions unknown")
    }

    abstract fun toList(): List<T>

    fun toList(allOptions: () -> List<T>): List<T> =
        if (this is Any) {
            allOptions.invoke()
        } else {
            this.toList()
        }.toSet().toList()

    companion object {
        fun <T> fromList(list: List<T>): Solutions<T> {
            val set = list.toSet()
            return when (set.size) {
                0 -> None()
                1 -> One(set.first())
                else -> Some(set.toList())
            }
        }
    }
}

interface Attribute : NamedItem {

    val valueType: ValueType
    val cardinality: Cardinality
    val description: String

    interface Group : NamedItem.Group
}

enum class Cardinality : NamedItem {
    ONE,
    MANY
}

enum class Scheme : Attribute {

    NAME {
        override val valueType = ValueType.NAME
        override val cardinality = Cardinality.ONE
        override val description = "The unique name of an attribute."
    },
    VALUETYPE {
        override val valueType = ValueType.NAME
        override val cardinality = Cardinality.ONE
        override val description = "The type of the value that can be associated with a value."
    },
    CARDINALITY {
        override val valueType = ValueType.NAME
        override val cardinality = Cardinality.ONE
        override val description =
            "Specifies whether an attribute associates a single value or a set of values with an entity."
    },
    DESCRIPTION {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "Specifies a documentation string"
    }
}

internal fun Input.toBinder(): Binder<*> = when (value) {
    is Value.LONG -> Binder(label, { listOf(value.v) }, Value::LONG, Solutions.One(value.v))
    is Value.NAME -> Binder(label, { listOf(value.v) }, Value::NAME, Solutions.One(value.v))
    is Value.INSTANT -> Binder(label, { listOf(value.v) }, Value::INSTANT, Solutions.One(value.v))
    is Value.BOOLEAN -> Binder(label, { listOf(value.v) }, Value::BOOLEAN, Solutions.One(value.v))
    is Value.STRING -> Binder(label, { listOf(value.v) }, Value::STRING, Solutions.One(value.v))
    is Value.DOUBLE -> Binder(label, { listOf(value.v) }, Value::DOUBLE, Solutions.One(value.v))
    is Value.BIGDEC -> Binder(label, { listOf(value.v) }, Value::BIGDEC, Solutions.One(value.v))
    is Value.VALUE -> Binder(label, { listOf(value.v) }, Value::VALUE, Solutions.One(value.v))
    is Value.DATA -> Binder(label, { listOf(value.v) }, Value::DATA, Solutions.One(value.v))
}
