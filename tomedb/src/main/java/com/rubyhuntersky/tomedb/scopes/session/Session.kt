package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.database.Database

interface Session {
    fun getDb(): Database
    fun transactDb(updates: Set<Update>)
    fun close()
}
