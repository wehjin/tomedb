package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Meter
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact.Standing

interface Datalog {
    fun append(entity: Long, meter: Meter, value: Value, standing: Standing = Standing.Asserted): Fact
    val allEntities: List<Long>
    val allAssertedValues: List<Value>
    fun entityMeterValues(entity: Long, meter: Meter): List<Value>
    fun isEntityMeterValueAsserted(entity: Long, meter: Meter, value: Value): Boolean
    fun isEntityMeterAsserted(entity: Long, meter: Meter): Boolean
}
