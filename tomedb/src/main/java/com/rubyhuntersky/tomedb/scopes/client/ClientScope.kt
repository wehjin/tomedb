package com.rubyhuntersky.tomedb.scopes.client

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.tagOf
import com.rubyhuntersky.tomedb.connection.FileSession
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionMsg
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.util.*

@ScopeTagMarker
interface ClientScope : CoroutineScope, DestructuringScope {

    val dbDir: File
    val dbSpec: List<Attribute>

    fun connectToDatabase(): SessionScope {
        val channel = Channel<SessionMsg>(10)
        val job = launch(Dispatchers.IO) {
            val session = FileSession(dbDir, dbSpec)
            for (msg in channel) {
                processMsg(msg, session)
            }
        }
        return CommonDbSessionScope(SessionChannel(job, channel))
    }

    suspend fun processMsg(msg: SessionMsg, session: FileSession) {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (msg) {
            is SessionMsg.DB -> {
                val db = session.db()
                msg.backChannel.send(db)
            }
            is SessionMsg.UPDATE -> session.updateDb(msg.updates)
            is SessionMsg.BATCH -> {
                val ents = session.transactData(msg.tagLists)
                msg.backChannel.send(ents)
            }
            is SessionMsg.FIND -> {
                val db = session.db()
                val result = db.find(msg.query)
                msg.backChannel.send(result)
            }
            else -> TODO()
        }
    }

    private class CommonDbSessionScope(override val sessionChannel: SessionChannel) : SessionScope

    operator fun <T : Any> Keyword.rangeTo(value: T) = tagOf(value, this)
    operator fun Keyword.rangeTo(v: Boolean) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: Long) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: Int) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: Keyword) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: String) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: Date) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: Double) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: BigDecimal) = tagOf(v, this)
    operator fun Keyword.rangeTo(v: TagList) = tagOf(v, this)
}


interface DestructuringScope {
    operator fun Map<Keyword, *>.get(attr: Attribute) = this[attr.attrName]
}