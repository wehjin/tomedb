package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.scopes.query.DatabaseChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

data class SessionChannel(
    private val job: Job, private val channel: Channel<SessionMsg>
) {

    suspend fun send(updates: Set<Update>) = channel.send(
        SessionMsg.UPDATE(
            updates
        )
    )

    suspend fun batch(tagLists: List<TagList>): List<Long> {
        val resultChannel = Channel<List<Long>>()
        channel.send(SessionMsg.BATCH(tagLists, resultChannel))
        return resultChannel.receive()
    }

    suspend fun find(query: Query.Find2): FindResult {
        val resultChannel = Channel<FindResult>()
        channel.send(SessionMsg.FIND(query, resultChannel))
        return resultChannel.receive()
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