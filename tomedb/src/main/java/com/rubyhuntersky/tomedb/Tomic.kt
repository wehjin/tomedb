package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import java.io.File

interface Tomic {
    fun getDb(): Database
    fun write(forms: List<Form<*>>)
    fun close()
}

fun tomicOf(dir: File, init: TomicScope.() -> List<Attribute<*>>): Tomic {
    val spec = object : TomicScope {}.init()
    val session = startSession(dir, spec)
    return object : Tomic {
        override fun close() = session.close()
        override fun getDb(): Database = session.getDb()

        override fun write(forms: List<Form<*>>) {
            val updates = forms.map {
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
