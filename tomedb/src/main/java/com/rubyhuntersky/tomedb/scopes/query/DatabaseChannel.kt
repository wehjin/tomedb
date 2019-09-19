package com.rubyhuntersky.tomedb.scopes.query

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel

data class DatabaseChannel(val sessionChannel: SessionChannel) {
    suspend fun find2(query: Query.Find): FindResult = sessionChannel.find(query)
    suspend fun getValue(entity: Long, attr: Keyword): Any? = sessionChannel.getValue(entity, attr)
}