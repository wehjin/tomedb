package com.rubyhuntersky.tomedb.scopes.session

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity

interface Session {
    fun getDb(): Database
    fun transactDb(updates: Set<Update>)
    fun close()
}

fun Session.transact(attr: Attribute<*>, value: Any): Database {
    val update = Update(0, attr.toKeyword(), value)
    transactDb(setOf(update))
    return getDb()
}

fun <KeyT : Any> Session.transact(newEntity: Entity<KeyT>?, oldEntity: Entity<KeyT>?): Database {
    require(newEntity == null || newEntity.canReplace(oldEntity))
    val updates = when {
        newEntity != null && oldEntity == null -> newEntity.toUpdates()
        newEntity != null && oldEntity != null -> newEntity - oldEntity
        newEntity == null && oldEntity != null -> oldEntity.toUpdates().map(Update::retract)
        else -> emptyList()
    }
    transactDb(updates.toSet())
    return getDb()
}
