package com.rubyhuntersky.tomedb.datascope

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.connection.Connection
import com.rubyhuntersky.tomedb.connection.Database
import java.io.File

@TomeTagMarker
interface ConnectionScope {

    val dbDir: File
    val dbSpec: List<Attribute>

    fun connect(run: UpdateScope.() -> Unit): Connection =
        Client().connect(dbDir, dbSpec).also { CommonUpdateScope(it).apply(run) }
}

@TomeTagMarker
interface UpdateScope {

    val conn: Connection

    fun transact(updates: Set<Update>) = conn.send(updates)

    fun checkoutLatest(run: QueryScope.() -> Unit) {
        val db = conn.checkout()
        QueryScope(db, ::transact).apply(run)
    }
}

private class CommonUpdateScope internal constructor(override val conn: Connection) : UpdateScope

@TomeTagMarker
class QueryScope internal constructor(
    private val db: Database,
    private val sessionTransact: (Set<Update>) -> Unit
) {
    fun transact(updates: Set<Update>) = sessionTransact(updates)

    fun find(build: Query.Find2.() -> Unit): FindResult = db.find2(query(build))
    fun query(build: Query.Find2.() -> Unit): Query.Find2 = Query.Find2(build)

    fun slot(name: String): Query.Find2.Slot = Query.Find2.CommonSlot(name)
    operator fun String.unaryMinus(): Query.Find2.Slot = slot(this)
}

fun dataScope(dbDir: File, dbSpec: List<Attribute>): ConnectionScope = CommonConnectionScope(dbDir, dbSpec)

private class CommonConnectionScope(override val dbDir: File, override val dbSpec: List<Attribute>) : ConnectionScope
