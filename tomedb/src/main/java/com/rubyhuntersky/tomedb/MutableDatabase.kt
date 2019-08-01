package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.GitDatalog
import java.nio.file.Path

class MutableDatabase(dataDir: Path) {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun update(update: Update): Fact {
        val (entity, meter, value, type) = update
        require(value !is Value.DATA)
        return datalog.append(entity, meter, value, type.toStanding())
    }

    private val datalog: Datalog = GitDatalog(dataDir)

    private fun Update.Type.toStanding(): Fact.Standing = when (this) {
        Update.Type.Assert -> Fact.Standing.Asserted
        Update.Type.Retract -> Fact.Standing.Retracted
    }

    internal fun commit() {
        // Unused for now
    }

    operator fun invoke(query: Query): List<Map<String, Value>> {
        val (rules, inputs, outputs) = query as Query.Find
        val initBinders = inputs?.map(Input::toBinder)
        return BinderRack(initBinders).stir(outputs, rules, datalog)
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}