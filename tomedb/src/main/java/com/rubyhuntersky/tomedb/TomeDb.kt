package com.rubyhuntersky.tomedb

import java.util.*

class Client {
    fun connect(dbName: String) = Connection(dbName)
}


class Connection(val dbName: String) {

    fun transactAttributes(vararg attributes: Attribute) {
        transactData(attributes.map {
            mapOf(
                Pair(AttributeAttribute.NAME, Value.ATTRNAME(it.attrName))
                , Pair(AttributeAttribute.VALUETYPE, Value.ATTRNAME(it.valueType.toAttrName()))
                , Pair(AttributeAttribute.CARDINALITY, Value.ATTRNAME(it.cardinality.toAttrName()))
                , Pair(AttributeAttribute.DESCRIPTION, Value.STRING(it.description))
            )
        })
    }

    fun transactData(data: List<Map<out Enum<*>, Value>>) {
        val time = Date()
        data.forEach { attributes ->
            val entity = database.popEntity()
            attributes.forEach { attribute, value ->
                val attrName = attribute.toAttrName()
                database.addFact(entity, attrName, value, true, time)
            }
        }
    }

    val database = MutableDatabase()
}


sealed class Rule {
    data class EntitiesWithAttribute(val varName: String, val attribute: Enum<*>) : Rule()
    data class EntitiesWithAttributeValue(val binderName: String, val attribute: Enum<*>, val value: Value) : Rule()
}

sealed class Query {
    data class Find(val outputName: String, val rules: List<Rule>) : Query()
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

    fun toList(allOptions: () -> List<T>): List<T> = if (this is Solutions.Any) {
        allOptions.invoke()
    } else {
        this.toList()
    }

    companion object {
        fun <T> fromList(list: List<T>): Solutions<T> = when {
            list.isEmpty() -> Solutions.None()
            list.size == 1 -> Solutions.One(list[0])
            else -> Solutions.Some(list)
        }
    }
}

interface Attribute {
    val attrName: AttrName
        get() = (this as? Enum<*>)?.let { AttrName(this::javaClass.name, this.name) }
            ?: throw NotImplementedError("Attribute::attrName")
    val valueType: ValueType
    val cardinality: Cardinality
    val description: String
}

enum class ValueType {
    REF,
    ATTRNAME,
    DATE,
    BOOLEAN,
    STRING,
    LONG,
    DOUBLE,
    BIGDEC,
    VALUE,
    DATA,
}

enum class Cardinality {
    ONE,
    MANY
}

enum class AttributeAttribute {
    NAME,
    VALUETYPE,
    CARDINALITY,
    DESCRIPTION
}

fun Enum<*>.toAttrName(): AttrName = AttrName(this::javaClass.name, this.name)
