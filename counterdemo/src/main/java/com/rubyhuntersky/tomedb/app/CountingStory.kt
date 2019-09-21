package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.getDbValue
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import com.rubyhuntersky.tomedb.scopes.session.updateDb

class CountingStory(private val application: DemoApplication) : SessionScope {

    data class Mdl(val db: Database) {
        val count: Long by lazy { db.getDbValue(Counter.Count) ?: 42L }
    }

    sealed class Msg {
        object Incr : Msg()
        object Decr : Msg()
    }

    fun init(): Mdl = Mdl(db = getDb())

    fun update(mdl: Mdl, msg: Msg): Mdl {
        val newCount = when (msg) {
            Msg.Incr -> mdl.count + 1
            Msg.Decr -> mdl.count - 1
        }
        return mdl.copy(db = updateDb(Counter.Count, newCount))
    }

    override val sessionChannel
        get() = application.sessionScope.sessionChannel
}