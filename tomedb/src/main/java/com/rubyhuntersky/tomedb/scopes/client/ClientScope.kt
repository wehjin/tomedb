package com.rubyhuntersky.tomedb.scopes.client

import com.rubyhuntersky.tomedb.attributes.Attribute
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

@ScopeTagMarker
interface ClientScope : CoroutineScope {

    val dbDir: File
    val dbSpec: List<Attribute<*>>

    fun connectToDatabase(): SessionScope {
        val channel = Channel<SessionMsg>(10)
        val job = launch(Dispatchers.IO) {
            val session = FileSession(dbDir, dbSpec)
            for (msg in channel) {
                processMsg(msg, session)
            }
        }
        return CommonSessionScope(SessionChannel(job, channel))
    }

    suspend fun processMsg(msg: SessionMsg, session: FileSession) {
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

    private class CommonSessionScope(override val sessionChannel: SessionChannel) : SessionScope
}
