package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.scopes.query.dbGetValue
import com.rubyhuntersky.tomedb.scopes.session.ConnectionScope

class CountingStory(private val application: DemoApplication) : ConnectionScope {

    data class Mdl(val count: Long)

    sealed class Msg {
        object Incr : Msg()
        object Decr : Msg()
    }

    suspend fun init(): Mdl = Mdl(dbGetValue(Counter.Count) ?: 42L)

    suspend fun update(mdl: Mdl, msg: Msg): Mdl {
        val newCount = when (msg) {
            Msg.Incr -> mdl.count + 1
            Msg.Decr -> mdl.count - 1
        }
        dbSetValue(Counter.Count, newCount)
        return mdl.copy(count = newCount)
    }

    override val dbSessionChannel
        get() = application.connectionScope.dbSessionChannel
}