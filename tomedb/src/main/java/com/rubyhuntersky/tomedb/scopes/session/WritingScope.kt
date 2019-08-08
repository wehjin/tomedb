package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.data.*

interface WritingScope {

    suspend fun <KeyT : Any> dbWrite(page: Page<KeyT>) {
        val data = page.data
        val projections = data.bindEnt(page.subject.keyEnt)
        dbWrite(projections)
    }

    suspend fun <KeyT : Any> dbWrite(page: Page<KeyT>, line: Line<Any>): Page<KeyT> {
        val projection = line.bindEnt(page.subject.keyEnt)
        dbWrite(listOf(projection))
        return page + line
    }

    suspend fun dbWrite(facts: List<Projection<Any>>) {
        val updates = facts.map { Update(it.ent, it.attr, Value.of(it.value)) }
        transact(updates.toSet())
    }

    suspend fun <KeyT : Any> dbClear(page: Page<KeyT>) {
        when (val subject = page.subject) {
            is PageSubject.Entity -> TODO()
            is PageSubject.Follower, is PageSubject.TraitHolder -> {
                val update = Update(
                    subject.keyEnt.long,
                    subject.keyAttr!!.attrName,
                    Value.of(subject.keyValue!!),
                    Update.Action.valueOf(false)
                )
                transact(setOf(update))
            }
        }
    }

    suspend fun transact(updates: Set<Update>)

    fun Map<Keyword, Any>.bindEnt(ent: Ent): List<Projection<Any>> =
        this.entries.map { (attr, value) -> Projection(ent.long, attr, value) }
}
