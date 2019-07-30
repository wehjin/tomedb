package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

class Datalog {

    fun append(entity: Long, attrName: ItemName, value: Value, isAsserted: Boolean, time: Date) {
        println("APPEND $entity $attrName $value $isAsserted $time")
        val existing = eavt[entity]?.get(attrName)?.get(value)
        if (existing == null || existing.isAsserted != isAsserted) {
            val avt = eavt[entity]
                ?: mutableMapOf<ItemName, MutableMap<Value, Transaction>>().also {
                    eavt[entity] = it
                }
            val vt = avt[attrName]
                ?: mutableMapOf<Value, Transaction>().also {
                    avt[attrName] = it
                }
            vt[value] = Transaction(isAsserted, time)
        }
    }

    private val eavt =
        mutableMapOf<Long, MutableMap<ItemName, MutableMap<Value, Transaction>>>()

    data class Transaction(val isAsserted: Boolean, val time: Date)

    val entities: List<Long>
        get() = eavt.keys.toList()

    val values: List<Value>
        get() = eavt.values.asSequence()
            .map { it.values }.flatten()
            .map { it.entries }.flatten()
            .filter { it.value.isAsserted }
            .map { it.key }.distinct()
            .toList()

    fun listValues(entity: Long, attrName: ItemName): List<Value> {
        return eavt[entity]?.get(attrName)?.keys?.toList() ?: emptyList()
    }

    fun checkAsserted(entity: Long, attrName: ItemName, value: Value): Boolean {
        return eavt[entity]?.get(attrName)?.get(value)?.isAsserted ?: false
    }

    fun checkAttributeAsserted(entity: Long, attrName: ItemName): Boolean {
        return eavt[entity]?.get(attrName)?.values?.fold(false) { wasAsserted, nextValue ->
            if (wasAsserted) true else nextValue.isAsserted
        } ?: false
    }

    override fun toString(): String {
        return "Datalog(eavt=$eavt)"
    }
}