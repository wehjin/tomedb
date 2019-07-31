package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.Standing

class MutableDatabase(timeClock: TimeClock) {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun update(entity: Long, attrName: ItemName, value: Value, standing: Standing): Fact {
        require(value !is Value.DATA)
        return datalog.append(entity, attrName, value, standing)
    }

    private val datalog = Datalog(timeClock)

    operator fun get(query: Query): List<Map<String, Value>> {
        val find = query as Query.Find
        val initBinders = find.inputs?.map(Input::toBinder)
        return BinderRack(initBinders).stir(find.outputs, find.rules, datalog)
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}