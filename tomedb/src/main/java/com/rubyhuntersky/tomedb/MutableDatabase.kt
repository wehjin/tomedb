package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.TransientDatalog

class MutableDatabase(timeClock: TimeClock) {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun updateFact(action: FactAction): Fact {
        require(action.value !is Value.DATA)
        val (entity, attr, value, type) = action
        return datalog.append(entity, attr, value, type.toStanding())
    }

    private val datalog: Datalog = TransientDatalog(timeClock)

    operator fun get(query: Query): List<Map<String, Value>> {
        val find = query as Query.Find
        val initBinders = find.inputs?.map(Input::toBinder)
        return BinderRack(initBinders).stir(find.outputs, find.rules, datalog)
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}