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
            val entity = nextEntity++
            attributes.forEach { attribute, value ->
                val attrName = attribute.toAttrName()
                addFact(entity, attrName, value, time)
            }
        }
    }

    private fun addFact(entity: Long, attribute: AttrName, value: Value, time: Date) {
        val avt = entityAttributeValueTime[entity]
            ?: mutableMapOf<AttrName, MutableMap<Value, MutableList<Date>>>().also {
                entityAttributeValueTime[entity] = it
            }
        val vt = avt[attribute]
            ?: mutableMapOf<Value, MutableList<Date>>().also { avt[attribute] = it }
        val t = vt[value]
            ?: mutableListOf<Date>().also { vt[value] = it }
        t.add(0, time)
    }

    private var nextEntity: Long = 1

    private val entityAttributeValueTime =
        mutableMapOf<Long, MutableMap<AttrName, MutableMap<Value, MutableList<Date>>>>()
}

data class Change(
    val before: Database,
    val after: Database
)

data class Database(val id: String, val version: Long)

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
