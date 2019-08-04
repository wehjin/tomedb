package com.rubyhuntersky.tomedb.scopes.client

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.connection.Session
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
    val dbSpec: List<Attribute>

    fun connect(init: suspend SessionScope.() -> Unit): SessionChannel {
        val channel = Channel<SessionMsg>(10)
        val job = launch(Dispatchers.IO) {
            val session = Session(dbDir, dbSpec)
            while (true) {
                when (val msg = channel.receive()) {
                    is SessionMsg.UPDATE -> session.send(msg.updates)
                    is SessionMsg.BATCH -> {
                        val ents = session.transactData(msg.tagLists)
                        msg.backChannel.send(ents)
                        msg.backChannel.close()
                    }
                    is SessionMsg.FIND -> {
                        val db = session.checkout()
                        val result = db.find2(msg.query)
                        msg.backChannel.send(result)
                        msg.backChannel.close()
                    }
                }
            }
        }
        return SessionChannel(job, channel).also { sessionChannel ->
            launch(Dispatchers.Main) { init(CommonSessionScope(sessionChannel)) }
        }
    }

    private class CommonSessionScope(override val session: SessionChannel) : SessionScope
}


