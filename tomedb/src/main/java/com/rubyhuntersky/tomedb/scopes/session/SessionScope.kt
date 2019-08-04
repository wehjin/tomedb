package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.query.DatabaseChannel
import com.rubyhuntersky.tomedb.scopes.query.DatabaseScope
import com.rubyhuntersky.tomedb.scopes.query.ReadingScope

@ScopeTagMarker
interface SessionScope : ReadingScope, WritingScope {

    suspend fun withLiveDb(block: suspend DatabaseScope.() -> Unit) {
        block(DatabaseScope(databaseChannel, ::transact))
    }

    suspend fun checkoutLatest(block: suspend DatabaseScope.() -> Unit) {
        val dbScope = DatabaseScope(sessionChannel.checkout(), ::transact)
        block(dbScope)
    }

    override suspend fun transact(updates: Set<Update>) = sessionChannel.send(updates)

    val sessionChannel: SessionChannel

    override val databaseChannel: DatabaseChannel
        get() = DatabaseChannel(sessionChannel)
}

