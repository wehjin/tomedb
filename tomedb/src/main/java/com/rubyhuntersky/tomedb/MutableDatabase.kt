package com.rubyhuntersky.tomedb

import java.util.*

class MutableDatabase {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun addFact(entity: Long, attrName: AttrName, value: Value, isAsserted: Boolean, time: Date) {
        datalog.append(entity, attrName, value, isAsserted, time)
    }

    private val datalog = Datalog()

    fun query(query: Query): List<Map<String, Value>> {
        query as Query.Find
        return BinderRack().stir(query.outputVars, query.rules, datalog)
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}