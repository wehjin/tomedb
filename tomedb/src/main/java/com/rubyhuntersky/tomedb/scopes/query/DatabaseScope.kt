package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker

@ScopeTagMarker
class DatabaseScope internal constructor(
    private val databaseChannel: DatabaseChannel,
    private val sessionTransact: suspend (Set<Update>) -> Unit
) {
    suspend fun find(build: Query.Find2.() -> Unit): FindResult = databaseChannel.find2(query(build))

    fun query(build: Query.Find2.() -> Unit): Query.Find2 =
        Query.Find2(build)

    fun slot(name: String): Query.Find2.Slot =
        Query.CommonSlot(name)

    suspend fun transact(updates: Set<Update>) = sessionTransact(updates)

    operator fun String.unaryMinus(): Query.Find2.Slot = slot(this)
}