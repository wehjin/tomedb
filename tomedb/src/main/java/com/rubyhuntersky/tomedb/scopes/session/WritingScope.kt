package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

interface WritingScope {

    suspend fun <T : Any> setFact(ent: Long, attr: Keyword, v: T) {
        val update = Update(ent, attr, Value.of(v))
        transact(setOf(update))
    }

    suspend fun transact(updates: Set<Update>)
}