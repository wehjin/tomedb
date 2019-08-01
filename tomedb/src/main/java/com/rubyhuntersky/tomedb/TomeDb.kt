package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Attr
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.Value.*
import com.rubyhuntersky.tomedb.basics.ValueType

sealed class Rule {
    data class EExactM(val entityVar: String, val attr: Attr) : Rule()
    data class EExactVM(val entityVar: String, val value: Value<*>, val attr: Attr) : Rule()
    data class EEExactM(val startVar: String, val endVar: String, val attr: Attr) : Rule()
    data class EVExactM(val entityVar: String, val valueVar: String, val attr: Attr) : Rule()
}

data class Input(val label: String, val value: Value<*>) {
    constructor(label: String, long: Long) : this(label, LONG(long))
}

sealed class Query {
    data class Find(
        val rules: List<Rule>,
        val inputs: List<Input>? = null,
        val outputs: List<String>
    ) : Query()
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
        when {
            this is Any -> allOptions.invoke()
            else -> this.toList()
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

interface Attribute : Attr {
    val valueType: ValueType
    val cardinality: Cardinality
    val description: String
}

enum class Cardinality : Attr {
    ONE,
    MANY
}

enum class Scheme : Attribute {

    NAME {
        override val valueType = ValueType.ATTR
        override val cardinality = Cardinality.ONE
        override val description = "The unique name of an attribute."
    },
    VALUETYPE {
        override val valueType = ValueType.ATTR
        override val cardinality = Cardinality.ONE
        override val description = "The type of the value that can be associated with a value."
    },
    CARDINALITY {
        override val valueType = ValueType.ATTR
        override val cardinality = Cardinality.ONE
        override val description =
            "Specifies whether an attribute associates an entity with a single value or a set of values."
    },
    DESCRIPTION {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "Specifies a documentation string"
    }
}

internal fun Input.toBinder(): Binder<*> = when (value) {
    is LONG -> Binder(label, { listOf(value.v) }, ::LONG, Solutions.One(value.v))
    is ATTR -> Binder(label, { listOf(value.v) }, ::ATTR, Solutions.One(value.v))
    is INSTANT -> Binder(label, { listOf(value.v) }, ::INSTANT, Solutions.One(value.v))
    is BOOLEAN -> Binder(label, { listOf(value.v) }, ::BOOLEAN, Solutions.One(value.v))
    is STRING -> Binder(label, { listOf(value.v) }, ::STRING, Solutions.One(value.v))
    is DOUBLE -> Binder(label, { listOf(value.v) }, ::DOUBLE, Solutions.One(value.v))
    is BIGDEC -> Binder(label, { listOf(value.v) }, ::BIGDEC, Solutions.One(value.v))
    is VALUE -> Binder(label, { listOf(value.v) }, ::VALUE, Solutions.One(value.v))
    is DATA -> Binder(label, { listOf(value.v) }, ::DATA, Solutions.One(value.v))
}
