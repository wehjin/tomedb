package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity
import com.rubyhuntersky.tomedb.database.getDbEntities
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import com.rubyhuntersky.tomedb.scopes.session.updateDb
import java.util.*


class NotingStory(override val sessionChannel: SessionChannel) : SessionScope {

    data class Mdl(val db: Database) {
        val entities by lazy {
            db.getDbEntities<Date>(Note.CREATED).toList()
        }
    }

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class REVISE(val key: Date, val text: String) : Msg()
        data class DROP(val key: Date) : Msg()
    }

    fun init(): Mdl {
        return Mdl(db = getDb())
    }

    fun update(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val today = Date()
            val text = if (msg.text.isNotBlank()) msg.text else "Today is $today"
            val entity = Entity.from(Note.CREATED, today, mapOf(Note.TEXT to text))
            mdl.copy(db = updateDb(entity, null))
        }
        is Msg.REVISE -> {
            val target = mdl.entities.firstOrNull { it.key == msg.key }
            target?.let { oldEntity ->
                val newEntity = oldEntity.setValue(Note.TEXT, msg.text)
                mdl.copy(db = updateDb(newEntity, oldEntity))
            }
        }
        is Msg.DROP -> {
            val target = mdl.entities.firstOrNull { it.key == msg.key }
            target?.let { oldEntity ->
                mdl.copy(db = updateDb(null, oldEntity))
            }
        }
    }
}