package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.query.DatabaseScope

@ScopeTagMarker
interface SessionScope {

    val session: SessionChannel

    suspend fun transact(updates: Set<Update>) = session.send(updates)

    suspend fun checkoutMutable(init: suspend DatabaseScope.() -> Unit) {
        val scope = DatabaseScope(session.checkout(), ::transact)
        init(scope)
    }
}