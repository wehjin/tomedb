package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.data.*
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity
import com.rubyhuntersky.tomedb.scopes.query.dbTome
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import com.rubyhuntersky.tomedb.scopes.session.updateDb
import java.util.*


class NotingStory(override val sessionChannel: SessionChannel) : SessionScope {

    data class Mdl(val tome: Tome<Date>, val db: Database) {
        val entities by lazy {
            db.getDbEntities(Note.CREATED).toList()
        }
    }

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class REVISE(val key: Date, val text: String) : Msg()
        data class DROP(val key: Date) : Msg()
    }

    fun init(): Mdl {
        return Mdl(tome = dbTome(TomeTopic.Trait(Note.CREATED)), db = getDb())
    }

    fun update(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val date = Date()
            val text = if (msg.text.isBlank()) "Today is $date" else msg.text
            val entity = Entity.from(Note.CREATED, date, mapOf(Note.TEXT to text))
            val newDb = updateDb(entity)
            mdl.copy(tome = mdl.tome + entity.page, db = newDb)
        }
        is Msg.REVISE -> {
            mdl.tome(msg.key)?.let {
                val textLine = lineOf(Note.TEXT, msg.text)
                val nextPage = dbWrite(it, textLine)
                mdl.copy(tome = mdl.tome + nextPage, db = getDb())
            } ?: mdl
        }
        is Msg.DROP -> {
            mdl.tome(msg.key)?.let {
                dbClear(it)
                mdl.copy(tome = mdl.tome - msg.key, db = getDb())
            }
        }
    }
}