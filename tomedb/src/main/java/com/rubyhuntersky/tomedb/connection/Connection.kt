package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.MutableDatabase
import com.rubyhuntersky.tomedb.Scheme
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.NamedItem
import com.rubyhuntersky.tomedb.basics.Value
import java.nio.file.Path
import java.util.*

class Connection(dataPath: Path, starter: ConnectionStarter) {

    val database = MutableDatabase(dataPath)

    init {
        when (starter) {
            is ConnectionStarter.Attributes -> transactAttributes(starter.attributes)
            is ConnectionStarter.None -> Unit
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

    fun update(entity: Long, attribute: NamedItem, value: Value, isAsserted: Boolean = true) {
        addFact(entity, attribute.itemName, value, isAsserted)
    }

    fun commit() {
        database.commit()
    }
}