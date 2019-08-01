package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.AttrSpec
import com.rubyhuntersky.tomedb.MutableDatabase
import com.rubyhuntersky.tomedb.Scheme
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Attr
import com.rubyhuntersky.tomedb.basics.Value
import java.nio.file.Path
import java.util.*

class Connection(dataPath: Path, starter: ConnectionStarter) {

    val database = MutableDatabase(dataPath)

    init {
        when (starter) {
            is ConnectionStarter.AttrSpecs -> transactAttrSpecs(starter.attrs)
            is ConnectionStarter.None -> Unit
        }
    }

    private fun transactAttrSpecs(attrSpecs: List<AttrSpec>) {
        transactData(attrSpecs.map {
            listOf(
                Pair(Scheme.NAME, Value.NAME(it))
                , Pair(Scheme.VALUETYPE, Value.NAME(it.valueType))
                , Pair(Scheme.CARDINALITY, Value.NAME(it.cardinality))
                , Pair(Scheme.DESCRIPTION, Value.STRING(it.description))
            )
        })
    }

    private fun addFact(entity: Long, attr: Attr, value: Value, isAsserted: Boolean): Pair<Value, Date> {
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

    fun transactData(data: List<List<Pair<Attr, Value>>>): List<Long> {
        val entities = mutableListOf<Long>()
        data.forEach { meters ->
            val entity = database.nextEntity()
            meters.forEach { (meter, value) ->
                update(entity, meter, value)
            }
            entities.add(entity)
        }
        commit()
        return entities
    }

    fun update(entity: Long, attr: Attr, value: Value, isAsserted: Boolean = true) {
        addFact(entity, attr, value, isAsserted)
    }

    fun commit() {
        database.commit()
    }
}