package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.data.*
import com.rubyhuntersky.tomedb.scopes.query.dbTome
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import java.util.*

class NotingStory(override val sessionChannel: SessionChannel) :
    SessionScope {

    data class Mdl(val tome: Tome<Date>)

    sealed class Msg {
        object LIST : Msg()
        data class ADD(val text: String) : Msg()
        data class REVISE(val key: Date, val text: String) : Msg()
        data class DROP(val key: Date) : Msg()
    }

    fun init(): Mdl {
        val topic =
            TomeTopic.Trait<Date>(Note.CREATED)
        return Mdl(tome = dbTome(topic))
    }

    fun update(mdl: Mdl, msg: Msg): Mdl? = when (msg) {
        is Msg.LIST -> null
        is Msg.ADD -> {
            val date = Date()
            val page = pageOf(
                subject = mdl.tome.newPageSubject(date),
                data = mapOf(
                    Note.CREATED to date,
                    Note.TEXT to if (msg.text.isBlank()) "Today is $date" else msg.text
                )
            )
            dbWrite(page)
            mdl.copy(tome = mdl.tome + page)
        }
        is Msg.REVISE -> {
            mdl.tome(msg.key)?.let {
                val textLine = lineOf(
                    Note.TEXT,
                    msg.text
                )
                val nextPage = dbWrite(it, textLine)
                mdl.copy(tome = mdl.tome + nextPage)
            } ?: mdl
        }
        is Msg.DROP -> {
            mdl.tome(msg.key)?.let {
                dbClear(it)
                mdl.copy(tome = mdl.tome - msg.key)
            }
        }
    }
}