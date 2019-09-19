package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.database.Database

interface Session {
    fun db(): Database
    fun updateDb(updates: Set<Update>)
}

fun Session.setDbValue(attr: Attribute, value: Any): Database {
    updateDb(setOf(Update(0, attr.toKeyword(), value)))
    return db()
}
