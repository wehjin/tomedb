package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker

@ScopeTagMarker
interface SessionScope : Session {
    val session: Session
    override fun getDb() = session.getDb()
    override fun transactDb(updates: Set<Update>) = session.transactDb(updates)
    override fun close() = session.close()
}

fun <T> sessionScope(session: Session, block: SessionScope.() -> T): T {
    val scope = object : SessionScope {
        override val session = session
    }
    return scope.run(block)
}
