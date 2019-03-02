package com.rubyhuntersky.tomedb

class BinderRack {
    private val binders = mutableMapOf<String, Binder<*>>()

    data class Binder<T>(val name: String, var solutions: Solutions<T> = Solutions.Any())

    fun stir(
        outputs: List<String>,
        rules: List<Rule>,
        datalog: Datalog
    ): List<Value> {
        shake(rules, datalog)

        val outputVar = outputs.first()
        return binders[outputVar]!!.solutions.toList().map {
            when (it) {
                is Long -> Value.LONG(it)
                else -> throw Exception("Unexpected val in binder $it")
            }
        }
    }

    fun shake(rules: List<Rule>, datalog: Datalog) {
        rules.forEach {
            when (it) {
                is Rule.CollectEntitiesWithAttribute -> it.shake(datalog)
                is Rule.CollectEntitiesWithValue -> it.shake(datalog)
                is Rule.CollectEntitiesReferringToEntities -> it.shake(datalog)
                is Rule.CollectEntitiesAndValueWithAttributes -> it.shake(datalog)
            }
        }
    }

    private fun Rule.CollectEntitiesAndValueWithAttributes.shake(datalog: Datalog) {
        val entityBinder = addBinder<Long>(entityVar)
        val attrName = attribute.toAttrName()
        val valueBinder = addBinder<Value>(valueVar)
        val substitutions = entityBinder.solutions.toList { datalog.entities }
            .map { entity ->
                valueBinder.solutions.toList { datalog.listValues(entity, attrName) }
                    .map { value -> Pair(entity, value) }
            }
            .flatten()
            .filter { datalog.checkAsserted(it.first, attrName, it.second) }
        entityBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Value>::first))
        valueBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Value>::second))
    }

    private fun Rule.CollectEntitiesReferringToEntities.shake(datalog: Datalog) {
        val startBinder = addBinder<Long>(startVar)
        val endBinder = addBinder<Long>(endVar)
        val attrName = attribute.toAttrName()
        val substitutions =
            startBinder.solutions.toList { datalog.entities }
                .map { start: Long ->
                    endBinder.solutions.toList { datalog.entities }.map { end: Long -> Pair(start, end) }
                }
                .flatten()
                .filter { datalog.checkAsserted(it.first, attrName, Value.LONG(it.second)) }
        startBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Long>::first))
        endBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Long>::second))
    }

    private fun Rule.CollectEntitiesWithValue.shake(datalog: Datalog) {
        val entityBinder = addBinder<Long>(entityVar)
        val attrName = attribute.toAttrName()
        val value = value
        val matches = entityBinder.solutions.toList { datalog.entities }
            .filter { datalog.checkAsserted(it, attrName, value) }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun Rule.CollectEntitiesWithAttribute.shake(datalog: Datalog) {
        val entityBinder = addBinder<Long>(entityVar)
        val attrName = attribute.toAttrName()
        val matches = entityBinder.solutions.toList { datalog.entities }
            .filter { datalog.checkAttributeAsserted(it, attrName) }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun <T> addBinder(name: String): Binder<T> {
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