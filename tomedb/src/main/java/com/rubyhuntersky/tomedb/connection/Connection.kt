package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.MutableDatabase
import com.rubyhuntersky.tomedb.Scheme
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.*
import java.nio.file.Path
import java.util.*

class Connection(dataPath: Path, starter: ConnectionStarter) {

    val database = MutableDatabase(dataPath)

    init {
        when (starter) {
            is ConnectionStarter.Attributes -> transactAttributes(starter.attrs)
            is ConnectionStarter.None -> Unit
        }
    }

    private fun transactAttributes(attributes: List<Attribute>) {
        val data = attributes.map {
            tagListOf(
                it at Scheme.NAME,
                it.valueType at Scheme.VALUETYPE,
                it.cardinality at Scheme.CARDINALITY,
                it.description at Scheme.DESCRIPTION
            )
        }
        transactData(data)
    }

    private fun addFact(entity: Long, attr: Keyword, value: Value<*>, isAsserted: Boolean): Pair<Value<*>, Date> {
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

    fun transactData(data: List<TagList>): List<Long> {
        val entities = mutableListOf<Long>()
        data.forEach { tagList ->
            val entity = database.nextEntity()
            tagList.forEach { (value, keyword) ->
                update(entity, keyword, value)
            }
            entities.add(entity)
        }
        commit()
        return entities
    }

    fun update(entity: Long, attr: Keyword, value: Value<*>, isAsserted: Boolean = true) {
        addFact(entity, attr, value, isAsserted)
    }

    fun commit() {
        database.commit()
    }
}