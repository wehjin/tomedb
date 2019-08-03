package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.*

sealed class Rule {
    data class EntityContainsAttr(val entityVar: String, val attr: Keyword) : Rule()
    data class EntityContainsExactValueAtAttr(val entityVar: String, val value: Value<*>, val attr: Keyword) : Rule()
    data class EntityContainsAnyEntityAtAttr(val entityVar: String, val entityValueVar: String, val attr: Keyword) :
        Rule()

    data class EntityContainsAnyValueAtAttr(val entityVar: String, val valueVar: String, val attr: Keyword) : Rule()
}

data class Input<T : Any>(val label: String, val value: Value<T>) {

    internal fun toBinder(): Solver<T> = Solver(label, value.valueClass, { listOf(value) }, Solutions.One(value))
}

sealed class Solutions<out T : Any> {

    abstract fun toList(): List<Value<T>>

    object None : Solutions<Nothing>() {
        override fun toList(): List<Nothing> = emptyList()
    }

    data class One<T : Any>(val item: Value<T>) : Solutions<T>() {
        override fun toList(): List<Value<T>> = listOf(item)
    }

    data class Some<T : Any>(val items: List<Value<T>>) : Solutions<T>() {
        override fun toList(): List<Value<T>> = items
    }

    object All : Solutions<Nothing>() {
        override fun toList(): List<Nothing> = error("Solutions unknown")
    }

    companion object {
        fun <T : Any> fromList(list: List<Value<T>>): Solutions<T> {
            val set = list.toSet()
            return when (set.size) {
                0 -> None
                1 -> One(set.first())
                else -> Some(set.toList())
            }
        }
    }
}

interface Attribute : Keyword {
    val valueType: ValueType
    val cardinality: Cardinality
    val description: String

    val tagList: TagList
        get() = tagListOf(
            Scheme.NAME..this,
            valueType at Scheme.VALUETYPE,
            cardinality at Scheme.CARDINALITY,
            description at Scheme.DESCRIPTION
        )
}

enum class Cardinality : Keyword {
    ONE,
    MANY;

    override fun toString(): String = toKeywordString()
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
    };

    override fun toString(): String = toKeywordString()
}

