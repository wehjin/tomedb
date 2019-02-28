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
        val entityBinders = mutableMapOf<String, Binder<Long>>()
        query.rules.forEach { rule ->
            when (rule) {
                is Rule.EntitiesWithAttribute -> processEntityWithAttributeRule(rule, entityBinders)
                is Rule.EntitiesWithAttributeValue -> processEntitiesWithAttributeValueRule(rule, entityBinders)
            }
        }
        return entityBinders[query.outputName]!!.solutions.toList().map(Value::LONG)
    }

    data class Binder<T>(val name: String, var solutions: Solutions<T> = Solutions.Any())

    private fun processEntitiesWithAttributeValueRule(
        rule: Rule.EntitiesWithAttributeValue,
        entityBinders: MutableMap<String, Binder<Long>>
    ) {
        val entityBinder = entityBinders[rule.binderName]
            ?: Binder<Long>(rule.binderName).also { entityBinders[rule.binderName] = it }
        val attrName = rule.attribute.toAttrName()
        val value = rule.value
        val matches =
            entityBinder.solutions.toList(allOptions = { entityAttributeValueAsserted.keys.toList() })
                .filter {
                    entityAttributeValueAsserted[it]?.get(attrName)?.get(value)?.isAsserted
                        ?: false
                }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun processEntityWithAttributeRule(
        rule: Rule.EntitiesWithAttribute,
        entityBinders: MutableMap<String, Binder<Long>>
    ) {
        val entityBinder = entityBinders[rule.varName]
            ?: Binder<Long>(rule.varName).also { entityBinders[rule.varName] = it }
        val attrName = rule.attribute.toAttrName()
        val matches = entityBinder.solutions.toList(allOptions = { entityAttributeValueAsserted.keys.toList() })
            .filter {
                entityAttributeValueAsserted[it]?.get(attrName)?.values?.fold(false) { didMatch, next ->
                    if (didMatch) {
                        true
                    } else {
                        next.isAsserted
                    }
                } ?: false
            }
        entityBinder.solutions = Solutions.fromList(matches)
    }
}