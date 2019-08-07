package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Projection
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Ident
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

interface WritingScope {

    suspend fun <T : Any> dbSetFact(ent: Long, attr: Keyword, v: T) {
        val update = Update(ent, attr, Value.of(v))
        transact(setOf(update))
    }

    suspend fun <T : Any> dbWriteFact(ident: Ident, attr: Keyword, v: T) {
        val update = Update(ident.toEnt().long, attr, Value.of(v))
        transact(setOf(update))
    }

    suspend fun dbWrite(facts: List<Projection<Any>>) {
        val updates = facts.map { Update(it.ent, it.attr, Value.of(it.value)) }
        transact(updates.toSet())
    }

    suspend fun transact(updates: Set<Update>)

    fun Map<Keyword, Any>.bind(ent: Ent): List<Projection<Any>> =
        this.entries.map { (attr, value) -> Projection(ent.long, attr, value) }
}