package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.database.Database
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

sealed class SessionMsg {

    data class DB(
        val backChannel: SendChannel<Database>
    ) : SessionMsg()

    data class UPDATE(
        val updates: Set<Update>
    ) : SessionMsg()

    data class BATCH(
        val tagLists: List<TagList>,
        val backChannel: Channel<List<Long>>
    ) : SessionMsg()

    data class FIND(
        val query: Query.Find,
        val backChannel: Channel<FindResult>
    ) : SessionMsg()
}