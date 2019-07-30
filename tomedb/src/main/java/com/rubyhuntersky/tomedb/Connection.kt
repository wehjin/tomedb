package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.NamedItem
import com.rubyhuntersky.tomedb.basics.Value
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
                        addFact(entity, attr, value, isAsserted, time)
                    }
                }
            }
        }
    }

    private fun transactAttributes(attributes: List<Attribute>) {
        transactData(attributes.map {
            listOf(
                Pair(Scheme.NAME, Value.NAME(it.itemName))
                , Pair(Scheme.VALUETYPE, Value.NAME(it.valueType.itemName))
                , Pair(Scheme.CARDINALITY, Value.NAME(it.cardinality.itemName))
                , Pair(Scheme.DESCRIPTION, Value.STRING(it.description))
            )
        })
    }

    private fun addFact(entity: Long, attrName: ItemName, value: Value, isAsserted: Boolean, time: Date): Value {
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

    fun transactData(data: List<List<Pair<NamedItem, Value>>>): List<Long> {
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

    fun update(entity: Long, attribute: NamedItem, value: Value, time: Date = Date()) {
        val isAsserted = true
        val attrName = attribute.itemName
        val subValue = addFact(entity, attrName, value, isAsserted, time)
        writer.writeLine(Ledger.Line(entity, attrName, subValue, isAsserted, time))
    }

    fun commit() {
        writer.commit()
    }

}