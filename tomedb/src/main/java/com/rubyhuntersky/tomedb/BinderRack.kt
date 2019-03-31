package com.rubyhuntersky.tomedb

class BinderRack {
    private val binders = mutableMapOf<String, Binder<*>>()

    data class Binder<T>(
        val name: String,
        val allSolutions: () -> List<T>,
        val toValue: (T) -> Value,
        var solutions: Solutions<T> = Solutions.Any()
    ) {
        fun toValueList(): List<Value> = toList().map { toValue(it) }
        private fun toList(): List<T> = solutions.toList { allSolutions.invoke() }
        override fun toString(): String {
            return "Binder(name='$name', solutions=$solutions)"
        }
    }

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
            val value = it.second
            when (value) {
                is Value.LONG -> {
                    (locked[name] as Binder<Long>).solutions = Solutions.One(value.v)
                }
                is Value.ATTRNAME -> {
                    (locked[name] as Binder<AttrName>).solutions = Solutions.One(value.v)
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
                is Rule.EinA -> it.shake(datalog, binders)
                is Rule.EExactV -> it.shake(datalog, binders)
                is Rule.EE -> it.shake(datalog, binders)
                is Rule.EV -> it.shake(datalog, binders)
            }
        }
    }

    private fun Rule.EV.shake(datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        val entityBinder = binders.addBinder(entityVar, datalog::entities, Value::LONG)
        val attrName = attribute.toAttrName()
        val valueBinder = binders.addBinder(valueVar, datalog::values, Value::VALUE)
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

    private fun Rule.EE.shake(
        datalog: Datalog,
        binders: MutableMap<String, Binder<*>>
    ) {
        val startBinder = binders.addBinder(startVar, datalog::entities, Value::LONG)
        val endBinder = binders.addBinder(endVar, datalog::entities, Value::LONG)
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

    private fun Rule.EExactV.shake(datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        val entityBinder = binders.addBinder(entityVar, datalog::entities, Value::LONG)
        val attrName = attribute.toAttrName()
        val value = value
        val matches = entityBinder.solutions.toList { datalog.entities }
            .filter { datalog.checkAsserted(it, attrName, value) }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun Rule.EinA.shake(datalog: Datalog, binders: MutableMap<String, Binder<*>>) {
        val entityBinder = binders.addBinder(entityVar, datalog::entities, Value::LONG)
        val attrName = attribute.toAttrName()
        val matches = entityBinder.solutions.toList { datalog.entities }
            .filter { datalog.checkAttributeAsserted(it, attrName) }
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