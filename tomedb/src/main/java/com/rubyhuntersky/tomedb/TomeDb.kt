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
                database.addFact(entity, attrName, value, time)
            }
        }
    }

    val database = MutableDatabase()
}


sealed class Rule {
    data class AttributePresent(val entityBinderName: String, val attribute: Enum<*>) : Rule()
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

    companion object {
        fun <T> fromList(list: List<T>): Solutions<T> = when {
            list.isEmpty() -> Solutions.None()
            list.size == 1 -> Solutions.One(list[0])
            else -> Solutions.Some(list)
        }
    }
}

data class Binder<T>(val name: String, var solutions: Solutions<T> = Solutions.Any())

class MutableDatabase {
    private var nextEntity: Long = 1
    internal fun popEntity(): Long = nextEntity++

    internal fun addFact(entity: Long, attrName: AttrName, value: Value?, time: Date) {
        val existingAtom = eavt[entity]?.get(attrName)?.get(0)
        if (existingAtom == null || existingAtom.value != value) {
            val avt = eavt[entity]
                ?: mutableMapOf<AttrName, MutableList<ValueAtom>>().also { eavt[entity] = it }
            val vt = avt[attrName]
                ?: mutableListOf<ValueAtom>().also { avt[attrName] = it }
            vt.add(0, ValueAtom(value, time))
        }
    }

    private val eavt = mutableMapOf<Long, MutableMap<AttrName, MutableList<ValueAtom>>>()

    fun query(query: Query): List<Value> {
        query as Query.Find
        val entityBinders = mutableMapOf<String, Binder<Long>>()
        query.rules.forEach { rule ->
            rule as Rule.AttributePresent
            val entityBinderName = rule.entityBinderName
            val entityBinder = entityBinders[entityBinderName]
                ?: Binder<Long>(entityBinderName).also { entityBinders[entityBinderName] = it }
            val attrName = rule.attribute.toAttrName()
            val entitySolutions = entityBinder.solutions
            val domain = when (entitySolutions) {
                is Solutions.None -> emptyList()
                is Solutions.One -> listOf(entitySolutions.item)
                is Solutions.Some -> entitySolutions.items
                is Solutions.Any -> eavt.keys.toList()
            }
            val range = domain.filter {
                eavt[it]?.get(attrName)?.get(0)?.value != null
            }
            entityBinder.solutions = Solutions.fromList(range)
        }
        val outputName = query.outputName
        return entityBinders[outputName]!!.solutions.toList().map {
            Value.LONG(it)
        }
    }
}

data class ValueAtom(
    val value: Value?,
    val time: Date
)

interface Attribute {
    val attrName: AttrName
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
