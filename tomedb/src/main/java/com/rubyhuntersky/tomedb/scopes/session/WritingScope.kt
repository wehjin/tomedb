package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update

interface WritingScope {

    fun transactDb(updates: Set<Update>)
}
