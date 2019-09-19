package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import kotlinx.coroutines.channels.Channel

sealed class SessionMsg {

    data class UPDATE(
        val updates: Set<Update>
    ) : SessionMsg()

    data class BATCH(
        val tagLists: List<TagList>,
        val backChannel: Channel<List<Long>>
    ) : SessionMsg()

    data class VALUE(
        val entity: Long,
        val attr: Keyword,
        val backChannel: Channel<Any?>
    ) : SessionMsg()

    data class FIND(
        val query: Query.Find,
        val backChannel: Channel<FindResult>
    ) : SessionMsg()
}