package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.attributes.invoke
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.scopes.session.Session
import com.rubyhuntersky.tomedb.scopes.session.sessionScope
import com.rubyhuntersky.tomedb.scopes.session.transact

data class CountingMdl(val db: Database) {
    val count: Long by lazy { Counter.Count(db) ?: 42L }
}

sealed class CountingMsg {
    object Incr : CountingMsg()
    object Decr : CountingMsg()
}

fun countingStory(session: Session): Pair<CountingMdl, (CountingMdl, CountingMsg) -> CountingMdl> {
    return sessionScope(session) {
        val init = CountingMdl(db = getDb())
        fun update(mdl: CountingMdl, msg: CountingMsg): CountingMdl {
            transact(
                attr = Counter.Count,
                value = when (msg) {
                    CountingMsg.Incr -> mdl.count + 1
                    CountingMsg.Decr -> mdl.count - 1
                }
            )
            return mdl.copy(db = getDb())
        }
        Pair(init, ::update)
    }
}
