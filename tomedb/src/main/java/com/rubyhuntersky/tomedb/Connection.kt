package com.rubyhuntersky.tomedb

import java.util.*

class Connection(private val writer: Ledger.Writer, starter: ConnectionStarter) {

    val database = MutableDatabase()

    init {
        when (starter) {
            is ConnectionStarter.Attributes -> transactAttributes(starter.attributes)
            is ConnectionStarter.Data -> {
                val reader = starter.reader
                while (reader.linesUnread > 0) {
                    val line = reader.readLine()
                    with(line) {
                        addFact(entity, attrName, value, isAsserted, time)
                    }
                }
            }
        }
    }

    private fun transactAttributes(attributes: List<Attribute>) {
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

    private fun addFact(entity: Long, attrName: AttrName, value: Value, isAsserted: Boolean, time: Date): Value {
        val subValue = if (value is Value.DATA) {
            val subData = listOf(value.v)
            val subEntities = transactData(subData)
            Value.LONG(subEntities.first())
        } else {
            value
        }
        database.addFact(entity, attrName, subValue, isAsserted, time)
        return subValue
    }

    fun transactData(data: List<List<Pair<Enum<*>, Value>>>): List<Long> {
        val time = Date()
        val entities = mutableListOf<Long>()
        data.forEach { attributes ->
            val entity = database.nextEntity()
            attributes.forEach {
                val attribute = it.first
                val value = it.second
                update(entity, attribute, value, time)
            }
            entities.add(entity)
        }
        commit()
        return entities
    }

    fun update(entity: Long, attribute: Enum<*>, value: Value, time: Date = Date()) {
        val isAsserted = true
        val attrName = attribute.toAttrName()
        val subValue = addFact(entity, attrName, value, isAsserted, time)
        writer.writeLine(Ledger.Line(entity, attrName, subValue, isAsserted, time))
    }

    fun commit() {
        writer.commit()
    }

}