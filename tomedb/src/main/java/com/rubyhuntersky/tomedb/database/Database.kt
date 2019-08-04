package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query

interface Database {
    fun find2(query: Query.Find2): FindResult
}