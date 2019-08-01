package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.TransientDatalog

class MutableDatabase(timeClock: TimeClock) {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun update(update: Update): Fact {
        val (entity, attr, value, type) = update
        require(value !is Value.DATA)
        return datalog.append(entity, attr, value, type.toStanding())
    }

    private val datalog: Datalog = TransientDatalog(timeClock)

    private fun Update.Type.toStanding(): Fact.Standing = when (this) {
        Update.Type.Assert -> Fact.Standing.Asserted
        Update.Type.Retract -> Fact.Standing.Retracted
    }

    operator fun invoke(query: Query): List<Map<String, Value>> {
        val find = query as Query.Find
        val initBinders = find.inputs?.map(Input::toBinder)
        return BinderRack(initBinders).stir(find.outputs, find.rules, datalog)
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}