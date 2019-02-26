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
