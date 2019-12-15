package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity
import com.rubyhuntersky.tomedb.database.entitiesWith
import java.util.*


class NotingStory(private val tomic: Tomic<Edit>) {

    data class Mdl(val db: Database) {
        val entities by lazy {
            db.entitiesWith(Note.CREATED).toList()
        }
    }

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class REVISE(val key: Date, val text: String) : Msg()
        data class DROP(val key: Date) : Msg()
    }

    fun init(): Mdl {
        return Mdl(db = tomic.readLatest())
    }

    fun update(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val today = Date()
            val text = if (msg.text.isNotBlank()) msg.text else "Today is $today"
            val entity = Entity.from(Note.CREATED, today, mapOf(Note.TEXT.attrName to text))
            tomic.write(edit = Edit.WriteNote(entity, null))
            mdl.copy(db = tomic.readLatest())
        }
        is Msg.REVISE -> {
            val target = mdl.entities.firstOrNull { it.key == msg.key }
            target?.let { oldEntity ->
                val newEntity = oldEntity.setValue(Note.TEXT, msg.text)
                tomic.write(edit = Edit.WriteNote(newEntity, oldEntity))
                mdl.copy(db = tomic.readLatest())
            }
        }
        is Msg.DROP -> {
            val target = mdl.entities.firstOrNull { it.key == msg.key }
            target?.let { oldEntity ->
                tomic.write(edit = Edit.WriteNote(null, oldEntity))
                mdl.copy(db = tomic.readLatest())
            }
        }
    }
}