package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import java.io.File

interface Tomic {
    val latest: Database
    fun write(reforms: List<Form<*>>)
    fun close()
}

fun tomicOf(dir: File, init: TomicScope.() -> List<Attribute<*>>): Tomic {
    val spec = object : TomicScope {}.init()
    val session = startSession(dir, spec)
    return object : Tomic {
        override fun close() = session.close()
        override val latest: Database
            get() = session.getDb()

        override fun write(reforms: List<Form<*>>) {
            val updates = reforms.map {
                val updateType = when (it) {
                    is Form.Set -> UpdateType.Declare
                    is Form.Clear -> UpdateType.Retract
                }
                Update(it.ent, it.attribute.toKeyword(), it.quantAsScript(), updateType)
            }
            session.transactDb(updates.toSet())
        }
    }
}

interface TomicScope
