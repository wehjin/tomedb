package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.scopes.query.DatabaseChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

data class SessionChannel(private val job: Job, private val channel: Channel<SessionMsg>) {

    suspend fun send(updates: Set<Update>) =
        channel.send(SessionMsg.UPDATE(updates))

    suspend fun batch(tagLists: List<TagList>): List<Long> =
        Channel<List<Long>>()
            .also { channel.send(SessionMsg.BATCH(tagLists, it)) }
            .receive()

    suspend fun getValue(entity: Long, attr: Keyword): Any? =
        Channel<Any?>()
            .also { channel.send(SessionMsg.VALUE(entity, attr, it)) }
            .receive()

    suspend fun find(query: Query.Find): FindResult =
        Channel<FindResult>()
            .also { channel.send(SessionMsg.FIND(query, it)) }
            .receive()

    fun checkout(): DatabaseChannel =
        DatabaseChannel(this)

    fun close() {
        channel.close()
        runBlocking {
            job.join()
        }
    }
}