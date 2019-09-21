package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.database.Database
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

data class SessionChannel(private val job: Job, private val channel: Channel<SessionMsg>) :
    Session {

    override fun getDb(): Database = runBlocking {
        Channel<Database>()
            .also { channel.send(SessionMsg.GetDb(it)) }
            .receive()
    }

    override fun transactDb(updates: Set<Update>) = runBlocking {
        channel.send(SessionMsg.UpdateDb(updates))
    }

    fun find(query: Query.Find): FindResult = runBlocking {
        Channel<FindResult>()
            .also { channel.send(SessionMsg.FIND(query, it)) }
            .receive()
    }

    fun close() {
        channel.close()
        runBlocking {
            job.join()
        }
    }
}