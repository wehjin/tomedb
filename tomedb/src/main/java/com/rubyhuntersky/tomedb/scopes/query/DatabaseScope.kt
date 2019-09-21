package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.session.WritingScope

@ScopeTagMarker
class DatabaseScope(
    override val databaseChannel: DatabaseChannel,
    private val sessionTransact: (Set<Update>) -> Unit
) : ReadingScope, WritingScope {

    override fun transactDb(updates: Set<Update>) = sessionTransact(updates)
}

