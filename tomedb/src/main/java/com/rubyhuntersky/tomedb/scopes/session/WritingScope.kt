package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.data.*

interface WritingScope {

    suspend fun <T : Any> dbClear(ent: Long, attr: Keyword, v: T) = dbSet(ent, attr, v, false)

    suspend fun <KeyT : Any> dbClear(page: Page<KeyT>) {
        when (val subject = page.subject) {
            is PageSubject.Entity -> TODO()
            is PageSubject.Follower, is PageSubject.TraitHolder -> dbClear(
                subject.keyEnt.long,
                subject.keyAttr!!.attrName,
                subject.keyValue!!
            )
        }
    }

    suspend fun <T : Any> dbSet(ent: Long, attr: Keyword, v: T, assert: Boolean = true) {
        val update = Update(ent, attr, Value.of(v), Update.Action.valueOf(assert))
        transact(setOf(update))
    }

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

    suspend fun transact(updates: Set<Update>)

    fun Map<Keyword, Any>.bindEnt(ent: Ent): List<Projection<Any>> =
        this.entries.map { (attr, value) -> Projection(ent.long, attr, value) }
}
