package com.rubyhuntersky.tomedb

import java.util.*

class Connection(private val writer: Ledger.Writer, private val reader: Ledger.Reader? = null) {

    fun transactAttributes(vararg attributes: Attribute) {
        transactData(attributes.map {
            listOf(
                Pair(Scheme.NAME, Value.ATTRNAME(it.attrName))
                , Pair(
                    Scheme.VALUETYPE,
                    Value.ATTRNAME(it.valueType.toAttrName())
                )
                , Pair(
                    Scheme.CARDINALITY,
                    Value.ATTRNAME(it.cardinality.toAttrName())
                )
                , Pair(
                    Scheme.DESCRIPTION,
                    Value.STRING(it.description)
                )
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
                val isAsserted = true
                database.addFact(entity, attribute.toAttrName(), subValue, isAsserted, time)
                writer.writeLine(Ledger.Line(entity, attribute.toAttrName(), subValue, isAsserted, time))
            }
            entities.add(entity)
        }
        writer.commit()
        return entities
    }

    val database = MutableDatabase()
}