package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.queryOf
import com.rubyhuntersky.tomedb.basics.valueFromData
import com.rubyhuntersky.tomedb.data.*
import com.rubyhuntersky.tomedb.scopes.client.DestructuringScope


inline fun <reified TraitT : Any> ReadingScope.dbTome(topic: TomeTopic.Trait<TraitT>): Tome<TraitT> {
    val traitHolders = dbRead(topic.attr).toList()
    val pages = traitHolders.map { traitHolder ->
        val data = dbRead(traitHolder)
        val title = topic.toSubject(valueFromData(topic.attr.attrName, data))
        pageOf(title, data)
    }
    return tomeOf(topic, pages.toSet())
}

interface ReadingScope : DestructuringScope {

    val databaseChannel: DatabaseChannel

    suspend fun dbRead(subject: PageSubject.Entity): Page<Ent> {
        val data = dbRead(subject.ent)
        return pageOf(subject, data)
    }

    fun dbRead(ent: Ent): Map<Keyword, Any> {
        val eSlot = slot("e")
        val aSlot = slot("a")
        val vSlot = slot("v")
        val query = queryOf {
            rules = listOf(
                +eSlot put (ent.long),
                eSlot has aSlot eq vSlot,
                -aSlot and vSlot
            )
        }
        return find(query).toProjections(ent, aSlot, vSlot).associateBy { it.attr }
            .mapValues { it.value.value }
    }

    fun dbRead(attr: Attribute): Sequence<Ent> {
        val eSlot = slot("e")
        val query = queryOf { rules = listOf(-eSlot, eSlot has attr.attrName) }
        val result = find(query)
        return result.toEnts(eSlot)
    }

    fun find(query: Query.Find): FindResult = databaseChannel.find2(query)

    fun slot(name: String): Query.Find.Slot = Query.CommonSlot(name)
}