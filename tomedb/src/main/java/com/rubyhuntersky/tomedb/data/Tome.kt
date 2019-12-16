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
                is SessionMsg.UpdateDb -> {
                    session.transactDb(msg.updates)
                }
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
