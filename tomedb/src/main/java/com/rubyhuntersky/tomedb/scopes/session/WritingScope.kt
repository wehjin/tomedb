package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

interface WritingScope {

    suspend fun <T : Any> assertFact(ent: Long, attr: Keyword, v: T) {
        val update = Update(ent, attr, Value.of(v))
        transact(setOf(update))
    }

    suspend fun <T : Any> replaceFact(ent: Long, attr: Keyword, v: T, vOld: T) {
        val updates = setOf(
            Update(
                ent,
                attr,
                Value.of(v),
                Update.Action.Declare
            ),
            Update(
                ent,
                attr,
                Value.of(vOld),
                Update.Action.Retract
            )
        )
        transact(updates)
    }

    suspend fun transact(updates: Set<Update>)
}