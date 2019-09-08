package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Fact.Standing

interface Datalog {

    fun ents(): Sequence<Long>

    fun attrs(entity: Long): Sequence<Keyword>
    fun attrs(): Sequence<Keyword>

    fun values(entity: Long, attr: Keyword): Sequence<Any>
    fun values(): Sequence<Any>

    fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean
    fun isAsserted(entity: Long, attr: Keyword): Boolean

    fun append(
        entity: Long,
        attr: Keyword,
        value: Any,
        standing: Standing = Standing.Asserted
    ): Fact

    fun commit()
}

fun Datalog.value(entity: Long, attr: Keyword): Any? =
    values(entity, attr).asSequence().firstOrNull { isAsserted(entity, attr, it) }
