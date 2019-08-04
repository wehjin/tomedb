package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.EntValue
import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Projection
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.basics.Keyword

interface ReadingScope {

    val databaseChannel: DatabaseChannel

    suspend operator fun Keyword.invoke(): Sequence<EntValue<*>> = findAttr(this)

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

    suspend fun find(query: Query.Find2): FindResult = databaseChannel.find2(query)

    fun query(build: Query.Find2.() -> Unit): Query.Find2 =
        Query.Find2(build)

    fun slot(name: String): Query.Find2.Slot = Query.CommonSlot(name)

    operator fun String.unaryMinus(): Query.Find2.Slot = slot(this)
}