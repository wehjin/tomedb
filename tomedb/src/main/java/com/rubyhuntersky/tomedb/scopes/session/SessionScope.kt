package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.query.DatabaseChannel
import com.rubyhuntersky.tomedb.scopes.query.DatabaseScope
import com.rubyhuntersky.tomedb.scopes.query.ReadingScope

@ScopeTagMarker
interface SessionScope : Session, ReadingScope, WritingScope {

    val sessionChannel: SessionChannel

    override val databaseChannel: DatabaseChannel
        get() = DatabaseChannel(sessionChannel)

    override fun getDb() = sessionChannel.getDb()

    override fun updateDb(updates: Set<Update>) = sessionChannel.updateDb(updates)

    operator fun <T> invoke(block: DatabaseScope.() -> T): T {
        return block(DatabaseScope(databaseChannel, ::updateDb))
    }
}

