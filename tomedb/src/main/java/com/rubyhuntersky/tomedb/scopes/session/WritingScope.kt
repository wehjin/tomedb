package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.*

interface WritingScope {

    fun <KeyT : Any> dbWrite(page: Page<KeyT>) {
        val data = page.data
        val projections = data.bindEnt(page.subject.keyEnt)
        dbWrite(projections)
    }

    fun <KeyT : Any> dbWrite(page: Page<KeyT>, vararg lines: Line<Any>): Page<KeyT> {
        val projection = lines.map { it.bindEnt(page.subject.keyEnt) }
        dbWrite(projection)
        return page + lines.toList()
    }

    fun dbWrite(facts: List<Projection<Any>>) {
        val updates = facts.map { Update(it.ent, it.attr, it.value) }
        updateDb(updates.toSet())
    }

    fun <KeyT : Any> dbClear(page: Page<KeyT>) {
        when (val subject = page.subject) {
            is PageSubject.Entity -> TODO()
            is PageSubject.Follower, is PageSubject.TraitHolder -> {
                val update = Update(
                    subject.keyEnt.long,
                    subject.keyAttr!!.attrName,
                    subject.keyValue!!,
                    Update.Action.valueOf(false)
                )
                updateDb(setOf(update))
            }
        }
    }

    fun updateDb(updates: Set<Update>)

    fun Map<Keyword, Any>.bindEnt(ent: Ent): List<Projection<Any>> =
        this.entries.map { (attr, value) -> Projection(ent.long, attr, value) }
}
