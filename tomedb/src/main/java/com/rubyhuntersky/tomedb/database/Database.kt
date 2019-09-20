package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword

inline fun <reified T : Any> Database.getDbValue(attr: Attribute): T? {
    return getUntypedDbValue(0L, attr.toKeyword()) as? T
}

interface Database {

    fun entityExistsWithAttrValue(attr: Keyword, value: Any): Boolean {
        return this { rules = listOf(-"e", "e" has attr eq value) }.isEmpty()
    }

    operator fun invoke(init: Query.Find.() -> Unit): List<Map<String, Any>> =
        this(Query.build(init))

    operator fun invoke(query: Query): List<Map<String, Any>> = find(query as Query.Find).toLegacy()

    fun find(query: Query.Find): FindResult

    fun getUntypedDbValue(entity: Long, attr: Keyword): Any?

    fun getDbEntities(attr: Attribute): Sequence<Entity>
}

