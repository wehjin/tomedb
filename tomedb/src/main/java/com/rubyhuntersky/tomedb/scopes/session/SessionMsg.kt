package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.database.Database
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

sealed class SessionMsg {

    data class GetDb(
        val backChannel: SendChannel<Database>
    ) : SessionMsg()

    data class UpdateDb(
        val updates: Set<Update>
    ) : SessionMsg()

    data class FIND(
        val query: Query.Find,
        val backChannel: Channel<FindResult>
    ) : SessionMsg()
}