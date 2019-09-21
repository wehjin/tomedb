package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity

interface Session {
    fun getDb(): Database
    fun transactDb(updates: Set<Update>)
}

fun Session.updateDb(attr: Attribute, value: Any): Database {
    val update = Update(0, attr.toKeyword(), value)
    transactDb(setOf(update))
    return getDb()
}

fun Session.updateDb(entity: Entity): Database {
    val updates = entity.toUpdates()
    transactDb(updates.toSet())
    return getDb()
}
