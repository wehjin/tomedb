package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword

interface Datalist {
    fun ents(attr: Keyword): Sequence<Long>
    fun ents(): Sequence<Long>

    fun attrs(entity: Long): Sequence<Keyword>
    fun attrs(): Sequence<Keyword>

    fun values(entity: Long, attr: Keyword): Sequence<Any>
    fun values(): Sequence<Any>

    fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean
    fun isAsserted(entity: Long, attr: Keyword): Boolean
}

interface Datalog : Datalist {

    fun append(
        entity: Long,
        attr: Keyword,
        value: Any,
        standing: Standing = Standing.Asserted
    ): Fact

    fun commit()

    fun toDatalist(): Datalist
}

fun Datalist.value(entity: Long, attr: Keyword): Any? =
    values(entity, attr).asSequence().firstOrNull { isAsserted(entity, attr, it) }

fun Datalist.attrValues(entity: Long): Sequence<Pair<Keyword, Any>> {
    val attrs = attrs(entity)
    return attrs.mapNotNull { attr ->
        value(entity, attr)?.let { value ->
            Pair(attr, value)
        }
    }
}
