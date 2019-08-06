package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.EntValue
import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Projection
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Ident
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.queryOf

interface ReadingScope {

    val databaseChannel: DatabaseChannel

    suspend fun entsWithAttr(attr: Keyword): Sequence<Ent> {
        val eSlot = slot("e")
        val query = queryOf { rules = listOf(-eSlot, eSlot has attr) }
        val result = find(query)
        return result.toEnts(eSlot)
    }

    suspend fun findFactsByAttrKey(attr: Keyword): Sequence<Projection<Any>> {
        val eSlot = slot("e")
        val aSlot = slot("a")
        val vSlot = slot("v")
        val query = queryOf {
            rules = listOf(eSlot has attr, eSlot has aSlot eq vSlot, -eSlot and aSlot and vSlot)
        }
        val result = find(query)
        return result.toProjections(eSlot, aSlot, vSlot)
    }

    suspend operator fun Keyword.invoke(ident: Ident): Any? = findValues(ident, this).firstOrNull()

    suspend fun findValues(ident: Ident, attr: Keyword): Sequence<Any> {
        val end = ident.toEnt().long
        return findAttr(attr).filter { it.ent == end }.map(EntValue<*>::value)
    }

    suspend fun findAttr(attr: Keyword): Sequence<EntValue<*>> {
        return projectAttr(attr).map(Projection<*>::toEntValue)
    }

    suspend fun projectAttr(attr: Keyword): Sequence<Projection<*>> {
        val eSlot = Query.CommonSlot("e")
        val vSlot = Query.CommonSlot("v")
        val query = query {
            rules = listOf(-eSlot and vSlot, eSlot has attr eq vSlot)
        }

        return find(query).toProjections(eSlot, attr, vSlot)
    }

    suspend fun find(query: Query.Find): FindResult = databaseChannel.find2(query)

    fun query(build: Query.Find.() -> Unit): Query.Find =
        Query.Find(build)

    fun slot(name: String): Query.Find.Slot = Query.CommonSlot(name)

    operator fun String.unaryMinus(): Query.Find.Slot = slot(this)
}