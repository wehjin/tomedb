package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.EntValue
import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Projection
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.basics.Ident
import com.rubyhuntersky.tomedb.basics.Keyword

interface ReadingScope {

    val databaseChannel: DatabaseChannel

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