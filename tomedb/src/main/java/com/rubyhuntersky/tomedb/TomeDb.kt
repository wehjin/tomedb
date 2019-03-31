package com.rubyhuntersky.tomedb

import java.util.*

class Client {
    fun connect(dbName: String) = Connection(dbName)
}

data class AttrUp(
    val value: Value,
    val attr: Enum<*>,
    val entity: Long? = null
)

data class EntityUp(
    val attrUps: List<AttrUp>,
    val entity: Long? = null
)


class Connection(val dbName: String) {

    fun transactAttributes(vararg attributes: Attribute) {
        transactData(attributes.map {
            listOf(
                Pair(Scheme.NAME, Value.ATTRNAME(it.attrName))
                , Pair(Scheme.VALUETYPE, Value.ATTRNAME(it.valueType.toAttrName()))
                , Pair(Scheme.CARDINALITY, Value.ATTRNAME(it.cardinality.toAttrName()))
                , Pair(Scheme.DESCRIPTION, Value.STRING(it.description))
            )
        })
    }

    fun transactData(data: List<List<Pair<Enum<*>, Value>>>): List<Long> {
        val time = Date()
        val entities = mutableListOf<Long>()
        data.forEach { attributes ->
            val entity = database.nextEntity()
            attributes.forEach {
                val attribute = it.first
                val value = it.second
                val subValue = if (value is Value.DATA) {
                    val subData = listOf(value.v)
                    val subEntities = transactData(subData)
                    Value.LONG(subEntities.first())
                } else {
                    value
                }
                database.addFact(entity, attribute.toAttrName(), subValue, true, time)
            }
            entities.add(entity)
        }
        return entities
    }

    val database = MutableDatabase()
}


sealed class Rule {
    data class EinA(val entityVar: String, val attribute: Enum<*>) : Rule()
    data class EExactV(val entityVar: String, val value: Value, val attribute: Enum<*>) : Rule()
    data class EE(val startVar: String, val endVar: String, val attribute: Enum<*>) : Rule()
    data class EV(val entityVar: String, val valueVar: String, val attribute: Enum<*>) : Rule()
}

data class Input(
    val label: String,
    val values: List<Value>
)

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
        if (this is Solutions.Any) {
            allOptions.invoke()
        } else {
            this.toList()
        }.toSet().toList()

    companion object {
        fun <T> fromList(list: List<T>): Solutions<T> = when {
            list.isEmpty() -> Solutions.None()
            list.size == 1 -> Solutions.One(list[0])
            else -> Solutions.Some(list.toSet().toList())
        }
    }
}

interface Attribute {
    val attrName: AttrName
        get() = (this as? Enum<*>)?.toAttrName()
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

enum class Scheme {
    NAME,
    VALUETYPE,
    CARDINALITY,
    DESCRIPTION
}

fun Enum<*>.toAttrName(): AttrName = AttrName(this::class.java.simpleName, this.name)
