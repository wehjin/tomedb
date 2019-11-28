package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker

@ScopeTagMarker
interface SessionScope : Session {
    val channel: SessionChannel
    override fun getDb() = channel.getDb()
    override fun transactDb(updates: Set<Update>) = channel.transactDb(updates)
}

fun SessionScope.cancel() {
    channel.close()
}
