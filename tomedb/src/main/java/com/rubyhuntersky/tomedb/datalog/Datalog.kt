package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Attr
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact.Standing

interface Datalog {
    fun append(entity: Long, attr: Attr, value: Value<*>, standing: Standing = Standing.Asserted): Fact
    val allEntities: List<Long>
    val allAssertedValues: List<Value<*>>
    fun entityAttrValues(entity: Long, attr: Attr): List<Value<*>>
    fun isEntityAttrValueAsserted(entity: Long, attr: Attr, value: Value<*>): Boolean
    fun isEntityAttrAsserted(entity: Long, attr: Attr): Boolean
}
