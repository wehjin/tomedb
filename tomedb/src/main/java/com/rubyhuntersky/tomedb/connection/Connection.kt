package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.NamedItem
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

class Connection(
    private val writer: Ledger.Writer,
    starter: ConnectionStarter,
    timeClock: TimeClock = TimeClock.REALTIME
) {

    val database = MutableDatabase(timeClock)

    init {
        when (starter) {
            is ConnectionStarter.Attributes -> transactAttributes(starter.attributes)
            is ConnectionStarter.Data -> {
                val reader = starter.reader
                while (reader.linesUnread > 0) {
                    val line = reader.readLine()
                    with(line) {
                        addFact(entity, attr, value, isAsserted)
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

    private fun addFact(entity: Long, attr: ItemName, value: Value, isAsserted: Boolean): Pair<Value, Date> {
        val subValue = if (value is Value.DATA) {
            val subData = listOf(value.v)
            val subEntities = transactData(subData)
            Value.LONG(subEntities.first())
        } else {
            value
        }
        val action = Update(entity, attr, subValue, Update.Type.valueOf(isAsserted))
        val time = database.update(action).inst
        return subValue to time
    }

    fun transactData(data: List<List<Pair<NamedItem, Value>>>): List<Long> {
        val entities = mutableListOf<Long>()
        data.forEach { attributes ->
            val entity = database.nextEntity()
            attributes.forEach {
                val attribute = it.first
                val value = it.second
                update(entity, attribute, value)
            }
            entities.add(entity)
        }
        commit()
        return entities
    }

    fun update(entity: Long, attribute: NamedItem, value: Value) {
        val isAsserted = true
        val attr = attribute.itemName
        val (subValue, time) = addFact(entity, attr, value, isAsserted)
        writer.writeLine(Ledger.Line(entity, attr, subValue, isAsserted, time))
    }

    fun commit() {
        writer.commit()
    }

}