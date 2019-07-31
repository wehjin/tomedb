package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Datalog

class BinderRack(initBinders: List<Binder<*>>?) {

    private val binders = initBinders?.associateBy { it.name }?.toMutableMap() ?: mutableMapOf()

    fun stir(outputs: List<String>, rules: List<Rule>, datalog: Datalog): List<Map<String, Value>> {
        shake(rules, datalog, binders)
        println("BINDERS after shake: $binders")
        val outputBinders = outputs.map { binders[it] ?: throw Exception("No binder for var $it") }
        val outputBindings = outputBinders.join(emptyList())
        val checkedOutputBindings = outputBindings.filter { join ->
            val lockedBinders = binders.lock(join)
            shake(rules, datalog, lockedBinders)
            val binderNames = join.map { it.first }
            binderNames.fold(true) { canSucceed, name ->
                if (!canSucceed) {
                    false
                } else {
                    lockedBinders[name]!!.solutions is Solutions.One
                }
            }
        }
        return checkedOutputBindings.map { bindings ->
            bindings.associate {
                val value = it.second
                val unwrapped = if (value is Value.VALUE) {
                    value.v
                } else {
                    value
                }
                Pair(it.first, unwrapped)
            }
        }
    }

    private fun MutableMap<String, Binder<*>>.lock(bindings: List<Pair<String, Value>>): MutableMap<String, Binder<*>> {
        val locked = this.entries.associate { Pair(it.key, it.value.copy()) }.toMutableMap()
        bindings.forEach {
            val name = it.first
            when (val value = it.second) {
                is Value.LONG -> {
                    (locked[name] as Binder<Long>).solutions = Solutions.One(value.v)
                }
                is Value.NAME -> {
                    (locked[name] as Binder<ItemName>).solutions = Solutions.One(value.v)
                }
                is Value.VALUE -> {
                    (locked[name] as Binder<Value>).solutions = Solutions.One(value.v)
                }
                else -> throw Exception("Invalid value $value with name $name")
            }
        }
        return locked
    }

    private fun List<Binder<*>>.join(prefixes: List<List<Pair<String, Value>>>): List<List<Pair<String, Value>>> {
        return if (this.isEmpty()) {
            prefixes
        } else {
            val head = this[0]
            val nameValues = head.toValueList().map { Pair(head.name, it) }
            val newPrefixes = if (prefixes.isEmpty()) {
                nameValues.map { nameValue ->
                    listOf(nameValue)
                }
            } else {
                prefixes.map { prefix ->
                    nameValues.map { nameValue ->
                        prefix + nameValue
                    }
                }.flatten()
            }
            return subList(1, size).join(newPrefixes)
        }
    }

    private fun shake(rules: List<Rule>, datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        rules.forEach {
            when (it) {
                is Rule.EExactA -> it.shake(datalog, binders)
                is Rule.EExactVA -> it.shake(datalog, binders)
                is Rule.EEExactA -> it.shake(datalog, binders)
                is Rule.EVExactA -> it.shake(datalog, binders)
            }
        }
    }

    private fun Rule.EVExactA.shake(datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        val entityBinder = binders.addBinder(entityVar, datalog::allEntities, Value::LONG)
        val valueBinder = binders.addBinder(valueVar, datalog::allValues, Value::VALUE)
        val substitutions = entityBinder.solutions.toList { datalog.allEntities }
            .map { entity ->
                valueBinder.solutions.toList { datalog.entityAttrValues(entity, attribute.itemName) }
                    .map { value -> Pair(entity, value) }
            }
            .flatten()
            .filter { datalog.isEntityAttrValueAsserted(it.first, attribute.itemName, it.second) }
        entityBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Value>::first))
        valueBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Value>::second))
    }

    private fun Rule.EEExactA.shake(
        datalog: Datalog,
        binders: MutableMap<String, Binder<*>>
    ) {
        val startBinder = binders.addBinder(startVar, datalog::allEntities, Value::LONG)
        val endBinder = binders.addBinder(endVar, datalog::allEntities, Value::LONG)
        val attrName = attribute.itemName
        val substitutions =
            startBinder.solutions.toList { datalog.allEntities }
                .map { start: Long ->
                    endBinder.solutions.toList { datalog.allEntities }.map { end: Long -> Pair(start, end) }
                }
                .flatten()
                .filter { datalog.isEntityAttrValueAsserted(it.first, attrName, Value.LONG(it.second)) }
        startBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Long>::first))
        endBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Long>::second))
    }

    private fun Rule.EExactVA.shake(datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        val entityBinder = binders.addBinder(entityVar, datalog::allEntities, Value::LONG)
        val attrName = attribute.itemName
        val value = value
        val matches = entityBinder.solutions.toList { datalog.allEntities }
            .filter { datalog.isEntityAttrValueAsserted(it, attrName, value) }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun Rule.EExactA.shake(datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        val entityBinder = binders.addBinder(entityVar, datalog::allEntities, Value::LONG)
        val attrName = attribute.itemName
        val matches = entityBinder.solutions.toList { datalog.allEntities }
            .filter { datalog.isEntityAttrAsserted(it, attrName) }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun <T> MutableMap<String, Binder<*>>.addBinder(
        name: String,
        allSolutions: () -> List<T>,
        toValue: (T) -> Value
    ): Binder<T> {
        val existing = this[name]
        return if (existing == null) {
            Binder(name, allSolutions, toValue).also {
                this[name] = it
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            existing as Binder<T>
        }
    }
}