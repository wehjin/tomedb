package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.scopes.query.DatabaseChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

data class SessionChannel(private val job: Job, private val channel: Channel<SessionMsg>) :
    Session {

    override fun db(): Database = runBlocking {
        Channel<Database>()
            .also { channel.send(SessionMsg.DB(it)) }
            .receive()
    }

    override fun updateDb(updates: Set<Update>) = runBlocking {
        channel.send(SessionMsg.UPDATE(updates))
    }

    suspend fun batch(tagLists: List<TagList>): List<Long> =
        Channel<List<Long>>()
            .also { channel.send(SessionMsg.BATCH(tagLists, it)) }
            .receive()

    fun find(query: Query.Find): FindResult = runBlocking {
        Channel<FindResult>()
            .also { channel.send(SessionMsg.FIND(query, it)) }
            .receive()
    }

    fun checkout(): DatabaseChannel =
        DatabaseChannel(this)

    fun close() {
        channel.close()
        runBlocking {
            job.join()
        }
    }
}