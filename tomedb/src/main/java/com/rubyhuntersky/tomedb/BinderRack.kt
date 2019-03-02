package com.rubyhuntersky.tomedb

class BinderRack {
    private val binders = mutableMapOf<String, Binder<*>>()

    data class Binder<T>(val name: String, var solutions: Solutions<T> = Solutions.Any())

    fun join(outputs: List<String>): List<Value> {
        val outputVar = outputs.first()
        return binders[outputVar]!!.solutions.toList().map {
            when (it) {
                is Long -> Value.LONG(it)
                else -> throw Exception("Unexpected val in binder $it")
            }
        }
    }

    fun shake(
        rules: List<Rule>,
        eava: MutableMap<Long, MutableMap<AttrName, MutableMap<Value, MutableDatabase.IsAssertedTime>>>
    ) {
        rules.forEach {
            when (it) {
                is Rule.CollectEntitiesWithAttribute -> it.shake(eava)
                is Rule.CollectEntitiesWithValue -> it.shake(eava)
            }
        }
    }

    private fun Rule.CollectEntitiesWithValue.shake(terms: MutableMap<Long, MutableMap<AttrName, MutableMap<Value, MutableDatabase.IsAssertedTime>>>) {
        val entityBinder = addBinder<Long>(entityVar)
        val attrName = attribute.toAttrName()
        val value = value
        val matches = entityBinder.solutions.toList(allOptions = { terms.keys.toList() })
            .filter {
                terms[it]?.get(attrName)?.get(value)?.isAsserted
                    ?: false
            }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun Rule.CollectEntitiesWithAttribute.shake(
        terms: MutableMap<Long, MutableMap<AttrName, MutableMap<Value, MutableDatabase.IsAssertedTime>>>
    ) {
        val entityBinder = addBinder<Long>(entityVar)
        val attrName = attribute.toAttrName()
        val matches = entityBinder.solutions.toList(allOptions = { terms.keys.toList() })
            .filter {
                terms[it]?.get(attrName)?.values?.fold(false) { didMatch, next ->
                    if (didMatch) {
                        true
                    } else {
                        next.isAsserted
                    }
                } ?: false
            }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    fun <T> addBinder(name: String): Binder<T> {
        val existing = binders[name]
        return if (existing == null) {
            val binder = Binder<T>(name)
            binders[name] = binder
            binder
        } else {
            @Suppress("UNCHECKED_CAST")
            existing as Binder<T>
        }
    }
}