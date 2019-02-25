package com.rubyhuntersky.tomedb

import java.util.*

class Client {
    fun connect(dbName: String) = Connection(dbName)
}


class Connection(val dbName: String) {
    fun addAttributes(vararg attributes: Attribute) {
        val now = Date()
        attributes.forEach {
            val attributeEntity = Value.ATTRNAME(it.attrName)
            addFact(attributeEntity, AttributeAttribute.NAME.toAttrId(), Value.ATTRNAME(it.attrName), now)
            addFact(
                attributeEntity,
                AttributeAttribute.VALUETYPE.toAttrId(),
                Value.ATTRNAME(it.valueType.toAttrId()),
                now
            )
            addFact(
                attributeEntity,
                AttributeAttribute.CARDINALITY.toAttrId(),
                Value.ATTRNAME(it.cardinality.toAttrId()),
                now
            )
            addFact(attributeEntity, AttributeAttribute.DESCRIPTION.toAttrId(), Value.STRING(it.description), now)
        }
    }

    private fun addFact(entity: Value, attribute: AttrName, value: Value, time: Date) {
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

    private val entityAttributeValueTime =
        mutableMapOf<Value, MutableMap<AttrName, MutableMap<Value, MutableList<Date>>>>()
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

fun <E : Enum<E>> Enum<E>.toAttrId(): AttrName = AttrName(this::javaClass.name, this.name)
