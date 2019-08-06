package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact.Standing

interface Datalog {
    fun append(entity: Long, attr: Keyword, value: Value<*>, standing: Standing = Standing.Asserted): Fact
    val allEntities: List<Long>
    val ents: Sequence<Long>
    val attrs: Sequence<Keyword>
    val values: Sequence<Value<*>>
        get() = allAssertedValues.asSequence()
    val allAssertedValues: List<Value<*>>
    fun values(entity: Long, attr: Keyword): Sequence<Value<*>>
    fun attrs(entity: Long): Sequence<Value<Keyword>>
    fun isAsserted(entity: Long, attr: Keyword, value: Value<*>): Boolean
    fun isAsserted(entity: Long, attr: Keyword): Boolean
    fun commit()

    fun assertedValueAtEntityAttr(entity: Long, attr: Keyword): Value<*>? =
        values(entity, attr).asSequence().firstOrNull { isAsserted(entity, attr, it) }
}
