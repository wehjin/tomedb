package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker

@ScopeTagMarker
interface SessionScope : Session {
    val sessionChannel: SessionChannel
    override fun getDb() = sessionChannel.getDb()
    override fun transactDb(updates: Set<Update>) = sessionChannel.transactDb(updates)
}
