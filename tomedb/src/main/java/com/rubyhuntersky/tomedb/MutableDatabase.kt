package com.rubyhuntersky.tomedb

import java.util.*

class MutableDatabase {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun addFact(entity: Long, attrName: AttrName, value: Value, isAsserted: Boolean, time: Date) {
        datalog.append(entity, attrName, value, isAsserted, time)
    }

    private val datalog = Datalog()

    operator fun get(query: Query): List<Map<String, Value>> {
        val find = query as Query.Find
        val initBinders = find.inputs?.map(Input::toBinder)
        return BinderRack(initBinders).stir(find.outputs, find.rules, datalog)
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}