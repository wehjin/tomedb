package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact.Standing

interface Datalog {

    fun append(entity: Long, attr: Keyword, value: Value<*>, standing: Standing = Standing.Asserted): Fact
    fun append(entity: Long, attr: Attribute, value: Value<*>, standing: Standing = Standing.Asserted) =
        append(entity, attr.attrName, value, standing)

    val allEntities: List<Long>
    val ents: Sequence<Long>
    val attrs: Sequence<Keyword>
    val values: Sequence<Value<*>>
        get() = allAssertedValues.asSequence()
    val allAssertedValues: List<Value<*>>
    fun values(entity: Long, attr: Keyword): Sequence<Value<*>>
    fun values(entity: Long, attr: Attribute) = values(entity, attr.attrName)
    fun attrs(entity: Long): Sequence<Value<Keyword>>
    fun isAsserted(entity: Long, attr: Keyword, value: Value<*>): Boolean
    fun isAsserted(entity: Long, attr: Attribute, value: Value<*>) = isAsserted(entity, attr.attrName, value)
    fun isAsserted(entity: Long, attr: Keyword): Boolean
    fun isAsserted(entity: Long, attr: Attribute) = isAsserted(entity, attr.attrName)
    fun commit()

    fun assertedValueAtEntityAttr(entity: Long, attr: Keyword): Value<*>? =
        values(entity, attr).asSequence().firstOrNull { isAsserted(entity, attr, it) }
}
