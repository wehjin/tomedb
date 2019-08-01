package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.MeterSpec
import com.rubyhuntersky.tomedb.MutableDatabase
import com.rubyhuntersky.tomedb.Scheme
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Meter
import com.rubyhuntersky.tomedb.basics.Value
import java.nio.file.Path
import java.util.*

class Connection(dataPath: Path, starter: ConnectionStarter) {

    val database = MutableDatabase(dataPath)

    init {
        when (starter) {
            is ConnectionStarter.MeterSpecs -> transactMeterSpecs(starter.meters)
            is ConnectionStarter.None -> Unit
        }
    }

    private fun transactMeterSpecs(meterSpecs: List<MeterSpec>) {
        transactData(meterSpecs.map {
            listOf(
                Pair(Scheme.NAME, Value.NAME(it))
                , Pair(Scheme.VALUETYPE, Value.NAME(it.valueType))
                , Pair(Scheme.CARDINALITY, Value.NAME(it.cardinality))
                , Pair(Scheme.DESCRIPTION, Value.STRING(it.description))
            )
        })
    }

    private fun addFact(entity: Long, meter: Meter, value: Value, isAsserted: Boolean): Pair<Value, Date> {
        val subValue = if (value is Value.DATA) {
            val subData = listOf(value.v)
            val subEntities = transactData(subData)
            Value.LONG(subEntities.first())
        } else {
            value
        }
        val action = Update(entity, meter, subValue, Update.Type.valueOf(isAsserted))
        val time = database.update(action).inst
        return subValue to time
    }

    fun transactData(data: List<List<Pair<Meter, Value>>>): List<Long> {
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

    fun update(entity: Long, meter: Meter, value: Value, isAsserted: Boolean = true) {
        addFact(entity, meter, value, isAsserted)
    }

    fun commit() {
        database.commit()
    }
}