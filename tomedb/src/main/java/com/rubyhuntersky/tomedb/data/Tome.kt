package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.connection.FileSession
import com.rubyhuntersky.tomedb.scopes.session.ChannelSession
import com.rubyhuntersky.tomedb.scopes.session.Session
import com.rubyhuntersky.tomedb.scopes.session.SessionMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File

/**
 * A Tome is a collection of pages each relating to
 * an entity described in a topic.
 */
@Deprecated(message = "Use Entities instead.")
data class Tome<KeyT : Any>(
    val topic: TomeTopic<KeyT>,
    val pages: Map<KeyT, Page<KeyT>>
)

val <KeyT : Any> Tome<KeyT>.size: Int
    get() = pages.size

operator fun <KeyT : Any> Tome<KeyT>.invoke(key: KeyT): Page<KeyT>? {
    return pages.values.asSequence().first { it.subject.key == key }
}

fun <KeyT : Any> tomeOf(topic: TomeTopic<KeyT>, pages: Set<Page<KeyT>>): Tome<KeyT> {
    return Tome(topic, pages.associateBy { it.key })
}

fun startSession(dbDir: File, dbSpec: List<Attribute<*>>): Session {
    val channel = Channel<SessionMsg>(10)
    val job = GlobalScope.launch(Dispatchers.IO) {
        val session = FileSession(dbDir, dbSpec)
        for (msg in channel) {
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when (msg) {
                is SessionMsg.GetDb -> {
                    val db = session.getDb()
                    msg.backChannel.send(db)
                }
                is SessionMsg.UpdateDb -> session.transactDb(msg.updates)
                is SessionMsg.FIND -> {
                    val db = session.getDb()
                    val result = db.find(msg.query)
                    msg.backChannel.send(result)
                }
                else -> TODO()
            }
        }
        session.close()
    }
    return ChannelSession(job, channel)
}
