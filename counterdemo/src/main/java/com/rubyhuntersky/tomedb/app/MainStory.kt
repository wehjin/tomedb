package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.app.CounterApplication.Edit
import com.rubyhuntersky.tomedb.attributes.invoke
import com.rubyhuntersky.tomedb.database.Database

data class CountingMdl(val db: Database) {
    val count: Long by lazy { Counter.Count(db) ?: 42L }
}

sealed class CountingMsg {
    object Incr : CountingMsg()
    object Decr : CountingMsg()
}

fun countingStory(tomic: Tomic<Edit>): Pair<CountingMdl, (CountingMdl, CountingMsg) -> CountingMdl> {
    val init = CountingMdl(db = tomic.getDb())
    fun update(mdl: CountingMdl, msg: CountingMsg): CountingMdl {
        val newCount = when (msg) {
            CountingMsg.Incr -> mdl.count + 1
            CountingMsg.Decr -> mdl.count - 1
        }
        tomic.write(Edit.Count(newCount))
        return mdl.copy(db = tomic.getDb())
    }
    return Pair(init, ::update)
}
