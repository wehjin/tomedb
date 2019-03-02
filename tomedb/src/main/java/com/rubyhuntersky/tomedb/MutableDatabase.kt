package com.rubyhuntersky.tomedb

import java.util.*

class MutableDatabase {
    private var nextEntity: Long = 1
    internal fun popEntity(): Long = nextEntity++

    internal fun addFact(entity: Long, attrName: AttrName, value: Value, isAsserted: Boolean, time: Date) {
        val existing = entityAttributeValueAsserted[entity]?.get(attrName)?.get(value)
        if (existing == null || existing.isAsserted != isAsserted) {
            val avt = entityAttributeValueAsserted[entity]
                ?: mutableMapOf<AttrName, MutableMap<Value, IsAssertedTime>>().also {
                    entityAttributeValueAsserted[entity] = it
                }
            val vt = avt[attrName]
                ?: mutableMapOf<Value, IsAssertedTime>().also {
                    avt[attrName] = it
                }
            vt[value] = IsAssertedTime(isAsserted, time)
        }
    }

    data class IsAssertedTime(val isAsserted: Boolean, val time: Date)

    private val entityAttributeValueAsserted =
        mutableMapOf<Long, MutableMap<AttrName, MutableMap<Value, IsAssertedTime>>>()

    fun query(query: Query): List<Value> {
        query as Query.Find
        val binderRack = BinderRack()
        binderRack.shake(query.rules, entityAttributeValueAsserted)
        return binderRack.join(query.outputVars)
    }
}