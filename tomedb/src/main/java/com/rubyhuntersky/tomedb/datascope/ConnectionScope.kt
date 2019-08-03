package com.rubyhuntersky.tomedb.datascope

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Client
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.connection.Connection
import com.rubyhuntersky.tomedb.connection.Database
import java.io.File

@TomeTagMarker
interface ConnectionScope {

    val dbDir: File
    val dbSpec: List<Attribute>

    fun connect(run: DataSession.() -> Unit) {
        val conn = Client().connect(dbDir, dbSpec)
        DataSession(conn).apply(run)
    }
}

@TomeTagMarker
class DataSession internal constructor(private val conn: Connection) {

    fun transact(updates: Set<Update>) = conn.send(updates)

    fun latest(run: DataReader.() -> Unit) {
        val db = conn.checkout()
        DataReader(db, ::transact).apply(run)
    }
}

@TomeTagMarker
class DataReader internal constructor(
    private val db: Database,
    private val sessionTransact: (Set<Update>) -> Unit
) {
    fun transact(updates: Set<Update>) = sessionTransact(updates)

    fun slot(name: String): Query.Find2.Slot = Query.Find2.CommonSlot(name)

    fun find(build: Query.Find2.() -> Unit): List<Map<String, Value<*>>> = db.find2(Query.Find2(build))
}

private class CommonConnectionScope(override val dbDir: File, override val dbSpec: List<Attribute>) : ConnectionScope

fun dataScope(dbDir: File, dbSpec: List<Attribute>): ConnectionScope = CommonConnectionScope(dbDir, dbSpec)