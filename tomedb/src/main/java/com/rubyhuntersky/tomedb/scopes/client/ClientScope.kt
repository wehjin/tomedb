package com.rubyhuntersky.tomedb.scopes.client

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.tagOf
import com.rubyhuntersky.tomedb.connection.FileSession
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionMsg
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
                when (msg) {
                    is SessionMsg.UPDATE -> session.send(msg.updates)
                    is SessionMsg.BATCH -> {
                        val ents = session.transactData(msg.tagLists)
                        msg.backChannel.send(ents)
                        msg.backChannel.close()
                    }
                    is SessionMsg.FIND -> {
                        val db = session.checkout()
                        val result = db.find(msg.query)
                        msg.backChannel.send(result)
                        msg.backChannel.close()
                    }
                    is SessionMsg.VALUE -> {
                        // Replace this msg with a DB message.
                        val db = session.checkout()
                        val value = db.getValue(msg.entity, msg.attr)
                        msg.backChannel.send(value)
                        msg.backChannel.close()
                    }
                }
            }
        }
        return CommonDbSessionScope(SessionChannel(job, channel))
    }

    private class CommonDbSessionScope(override val sessionChannel: SessionChannel) :
        SessionScope

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