package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.query.DatabaseChannel
import com.rubyhuntersky.tomedb.scopes.query.DatabaseScope
import com.rubyhuntersky.tomedb.scopes.query.ReadingScope

@ScopeTagMarker
interface ConnectionScope : ReadingScope, WritingScope {

    val dbSessionChannel: SessionChannel

    override val databaseChannel: DatabaseChannel
        get() = DatabaseChannel(dbSessionChannel)

    override suspend fun transact(updates: Set<Update>) = dbSessionChannel.send(updates)

    suspend operator fun <T> invoke(block: suspend DatabaseScope.() -> T): T {
        return block(DatabaseScope(databaseChannel, ::transact))
    }

    suspend fun checkoutLatest(block: suspend DatabaseScope.() -> Unit) {
        val dbScope = DatabaseScope(dbSessionChannel.checkout(), ::transact)
        block(dbScope)
    }
}

