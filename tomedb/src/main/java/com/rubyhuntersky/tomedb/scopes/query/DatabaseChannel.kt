package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel

data class DatabaseChannel(val sessionChannel: SessionChannel) {
    fun find2(query: Query.Find): FindResult = sessionChannel.find(query)
}