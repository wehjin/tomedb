package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query

interface Database {
    fun find(query: Query.Find): FindResult
}