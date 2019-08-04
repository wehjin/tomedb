package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.EntValue
import com.rubyhuntersky.tomedb.Projection
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.query.DatabaseScope

@ScopeTagMarker
interface SessionScope {

    val session: SessionChannel

    suspend fun transact(updates: Set<Update>) = session.send(updates)

    suspend fun withLiveDb(init: suspend DatabaseScope.() -> Unit) {
        val dbScope = DatabaseScope(session.checkout(), ::transact)
        init(dbScope)
    }

    suspend operator fun Keyword.invoke(): Sequence<EntValue<*>> = findAttr(this)

    suspend fun findAttr(attr: Keyword): Sequence<EntValue<*>> {
        return projectAttr(attr).map(Projection<*>::toEntValue)
    }

    suspend fun projectAttr(attr: Keyword): Sequence<Projection<*>> {
        val eSlot = Query.CommonSlot("e")
        val vSlot = Query.CommonSlot("v")
        val query = Query.build {
            rules = listOf(-eSlot and vSlot, eSlot has attr eq vSlot)
        }
        return session.find(query).toProjections(eSlot, attr, vSlot)
    }
}

