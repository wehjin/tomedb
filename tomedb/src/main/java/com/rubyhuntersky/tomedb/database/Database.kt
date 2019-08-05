package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.FindResult
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Value

interface Database {

    fun entityExistsWithAttrValue(attr: Scheme, value: Value<*>): Boolean {
        return this { rules = listOf(-"e", "e" has attr eq value) }.isEmpty()
    }

    operator fun invoke(init: Query.Find.() -> Unit): List<Map<String, Value<*>>> = this(Query.build(init))
    operator fun invoke(query: Query): List<Map<String, Value<*>>> = find(query as Query.Find).toLegacy()

    fun find(query: Query.Find): FindResult
}