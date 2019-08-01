package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact.Standing

interface Datalog {
    fun append(entity: Long, attr: ItemName, value: Value, standing: Standing = Standing.Asserted): Fact
    val allEntities: List<Long>
    val allAssertedValues: List<Value>
    fun entityAttrValues(entity: Long, attr: ItemName): List<Value>
    fun isEntityAttrValueAsserted(entity: Long, attr: ItemName, value: Value): Boolean
    fun isEntityAttrAsserted(entity: Long, attr: ItemName): Boolean
}
