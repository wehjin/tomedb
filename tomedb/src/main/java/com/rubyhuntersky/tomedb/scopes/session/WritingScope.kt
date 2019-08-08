package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Ident
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.data.*

interface WritingScope {

    suspend fun <T : Any> dbClear(ent: Long, attr: Attribute, v: T) = dbSet(ent, attr, v, false)
    suspend fun <T : Any> dbClear(ent: Long, attr: Keyword, v: T) = dbSet(ent, attr, v, false)

    suspend fun <T : Any> dbSet(ent: Long, attr: Attribute, v: T, assert: Boolean = true) =
        dbSet(ent, attr.attrName, v, assert)

    suspend fun <KeyT : Any> dbClear(page: Page<KeyT>) {
        when (val title = page.subject) {
            is PageSubject.Entity -> TODO()
            is PageSubject.Follower -> TODO()
            is PageSubject.TraitHolder<*> -> {
                val ent = title.traitHolder
                val attr = (title.topic as TomeTopic.Trait<*>).attr
                val value = title.traitValue
                dbClear(ent.long, attr.attrName, value)
            }
        }
    }

    suspend fun <T : Any> dbSet(ent: Long, attr: Keyword, v: T, assert: Boolean = true) {
        val update = Update(ent, attr, Value.of(v), Update.Action.valueOf(assert))
        transact(setOf(update))
    }

    suspend fun <KeyT : Any> dbWrite(page: Page<KeyT>) {
        val data = page.data
        val projections = data.bindEnt(page.subject.dataEnt)
        dbWrite(projections)
    }

    suspend fun <KeyT : Any> dbWrite(page: Page<KeyT>, line: Line<Any>): Page<KeyT> {
        val projection = line.bindEnt(page.subject.dataEnt)
        dbWrite(listOf(projection))
        return page + line
    }

    suspend fun dbWrite(facts: List<Projection<Any>>) {
        val updates = facts.map { Update(it.ent, it.attr, Value.of(it.value)) }
        transact(updates.toSet())
    }

    suspend fun <T : Any> dbWriteFact(ident: Ident, attr: Keyword, v: T) {
        val update = Update(ident.toEnt().long, attr, Value.of(v))
        transact(setOf(update))
    }

    suspend fun transact(updates: Set<Update>)

    fun Map<Keyword, Any>.bindEnt(ent: Ent): List<Projection<Any>> =
        this.entries.map { (attr, value) -> Projection(ent.long, attr, value) }
}
