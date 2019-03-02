package com.rubyhuntersky.tomedb

class Datalog(private val eava: Map<Long, MutableMap<AttrName, MutableMap<Value, MutableDatabase.IsAssertedTime>>>) {

    val entities: List<Long>
        get() = eava.keys.toList()

    fun listValues(entity: Long, attrName: AttrName): List<Value> {
        return eava[entity]?.get(attrName)?.keys?.toList() ?: emptyList()
    }

    fun checkAsserted(entity: Long, attrName: AttrName, value: Value): Boolean {
        return eava[entity]?.get(attrName)?.get(value)?.isAsserted ?: false
    }

    fun checkAttributeAsserted(entity: Long, attrName: AttrName): Boolean {
        return eava[entity]?.get(attrName)?.values?.fold(false) { wasAsserted, nextValue ->
            if (wasAsserted) true else nextValue.isAsserted
        } ?: false
    }
}