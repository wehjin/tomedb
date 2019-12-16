package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.basics.Keyword

interface Database {

    fun find(query: Query.Find): FindResult

    fun getUntypedDbValue(ent: Long, attr: Keyword): Any?

    fun <KeyT : Any> getDbEntitiesOfClass(
        attr: Attribute<*>,
        cls: Class<KeyT>
    ): Sequence<Entity<KeyT>>
}

inline fun <reified T : Any> Database.getDbValue(attr: Attribute<T>): T? {
    return getUntypedDbValue(0L, attr.toKeyword()) as? T
}

inline fun <reified KeyT : Any> Database.entitiesWith(attr: Attribute<KeyT>): Sequence<Entity<KeyT>> {
    return getDbEntitiesOfClass(attr, KeyT::class.java)
}


fun Database.entityExistsWithAttrValue(attr: Keyword, value: Any): Boolean {
    return query { rules = listOf(-"e", "e" has attr eq value) }.isEmpty()
}

fun Database.query(init: Query.Find.() -> Unit) =
    this.query(Query.build(init))

fun Database.query(query: Query): List<Map<String, Any>> {
    return find(query as Query.Find).toLegacy()
}


