package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Datalist

class BinderRack(initSolvers: List<Solver<*>>?) {

    private val solvers = initSolvers?.associateBy { it.name }?.toMutableMap() ?: mutableMapOf()

    fun stir(outputs: List<String>, rules: List<Rule>, datalog: Datalist): List<Map<String, Any>> {
        squeeze(rules, datalog, solvers)
        val outputSolvers = outputs.map { solvers[it] ?: error("No binder for output $it") }
        val outputBindings = outputSolvers.join(emptyList())

        val checkedOutputBindings = outputBindings
            .filter { outputBinding ->
                val constrainedBinders = solvers.constrainSolutions(outputBinding)
                squeeze(rules, datalog, constrainedBinders)
                val hasSingleSolutionPerOutput = outputBinding.map { (name, _) -> name }
                    .fold(true) { allPreviousBindersHaveOneSolution, name ->
                        allPreviousBindersHaveOneSolution && constrainedBinders[name]!!.possible is Possible.One<*>
                    }
                hasSingleSolutionPerOutput
            }

        return checkedOutputBindings.map { bindings ->
            bindings.associate { (name, value) -> Pair(name, value) }
        }
    }

    private fun MutableMap<String, Solver<*>>.constrainSolutions(bindings: List<Pair<String, Any>>): MutableMap<String, Solver<*>> {
        return mapValues { (_, binder) -> binder.copy() }.toMutableMap()
            .also {
                bindings.forEach { (name, value) ->
                    val solver = it[name] ?: error("No binder with name $name.")
                    it[name] = solver.setPossible(Possible.One(value))
                }
            }
    }

    private tailrec fun List<Solver<*>>.join(prefixes: List<List<Pair<String, Any>>>): List<List<Pair<String, Any>>> {
        return if (this.isEmpty()) prefixes
        else {
            val firstSolver = this[0]
            val valuesInFirstSolver = listValuesInSolver(firstSolver)
            val firstSolverNameAndValues = valuesInFirstSolver.map {
                Pair(firstSolver.name, it)
            }
            val newPrefixes =
                if (prefixes.isEmpty()) firstSolverNameAndValues.map { listOf(it) }
                else {
                    prefixes.map { prefix ->
                        firstSolverNameAndValues.map { nameValue -> prefix + nameValue }
                    }.flatten()
                }
            return subList(1, size).join(newPrefixes)
        }
    }

    private fun squeeze(
        rules: List<Rule>,
        datalog: Datalist,
        binders: MutableMap<String, Solver<*>>
    ) {
        rules.forEach {
            when (it) {
                is Rule.SlotAttr -> it.squeeze(datalog, binders)
                is Rule.SlotAttrValue -> it.squeeze(datalog, binders)
                is Rule.SlotAttrESlot -> it.squeeze(datalog, binders)
                is Rule.SlotAttrSlot -> it.squeeze(datalog, binders)
                is Rule.SlotSlotSlot -> it.squeeze(datalog, binders)
            }
        }
    }

    private fun Rule.SlotSlotSlot.squeeze(
        datalog: Datalist,
        binders: MutableMap<String, Solver<*>>
    ) {
        val entityBinder = addOrFindEntitySolver(this.entityVar, binders, datalog)
        val attrBinder = addOrFindAttrSolver(attrVar, binders, datalog)
        val valueBinder = addOrFindValueSolver(this.valueVar, binders, datalog)
        val ents = listPossible(entityBinder.possible, entityBinder.listAll)
        val entAttrs = ents.flatMap { entity ->
            listPossible(
                attrBinder.possible,
                listAll = { datalog.attrs(entity) }).map { Pair(entity, it) }
        }
        val entAttrValues = entAttrs
            .flatMap { (entity, attr) ->
                listPossible(valueBinder.possible, listAll = { datalog.values(entity, attr) })
                    .map { Triple(entity, attr, it) }
            }
            .filter { (e, a, v) -> datalog.isAsserted(e, a, v) }
        binders[entityVar] =
            entityBinder.setPossible(Possible.fromList(entAttrValues.map(Triple<Long, Keyword, Any>::first)))
        binders[attrVar] =
            attrBinder.setPossible(Possible.fromList(entAttrValues.map(Triple<Long, Keyword, Any>::second)))
        binders[valueVar] =
            valueBinder.setPossible(Possible.fromList(entAttrValues.map(Triple<Long, Keyword, Any>::third)))
    }

    private fun Rule.SlotAttrSlot.squeeze(
        datalog: Datalist,
        binders: MutableMap<String, Solver<*>>
    ) {
        val entityBinder = addOrFindEntitySolver(entityVar, binders, datalog)
        val valueBinder = addOrFindValueSolver(valueVar, binders, datalog)
        val ents = listPossible(entityBinder.possible, listAll = { datalog.ents().map { (it) } })
        val entValues = ents
            .flatMap { ent ->
                listPossible(
                    valueBinder.possible,
                    listAll = { datalog.values(ent, attr) }).map { Pair(ent, it) }
            }
            .filter { (entity, value) -> datalog.isAsserted(entity, attr, value) }

        binders[entityVar] =
            entityBinder.setPossible(Possible.fromList(entValues.map(Pair<Long, Any>::first)))
        binders[valueVar] =
            valueBinder.setPossible(Possible.fromList(entValues.map(Pair<Long, Any>::second)))
    }

    private fun Rule.SlotAttrESlot.squeeze(
        datalog: Datalist,
        binders: MutableMap<String, Solver<*>>
    ) {
        val startBinder = addOrFindEntitySolver(entityVar, binders, datalog)
        val endBinder = addOrFindEntitySolver(entityValueVar, binders, datalog)
        val substitutions =
            listPossible(startBinder.possible, listAll = { datalog.ents().map { (it) } })
                .flatMap { start: Long ->
                    listPossible(
                        possible = endBinder.possible,
                        listAll = { datalog.ents().map { (it) } }
                    ).map { end: Long -> Pair(start, end) }
                }
                .filter { datalog.isAsserted(it.first, attr, it.second) }

        binders[entityVar] =
            startBinder.setPossible(Possible.fromList(substitutions.map(Pair<Long, Any>::first)))
        binders[entityValueVar] =
            endBinder.setPossible(Possible.fromList(substitutions.map(Pair<Long, Any>::second)))
    }

    private fun Rule.SlotAttrValue.squeeze(
        datalog: Datalist,
        solvers: MutableMap<String, Solver<*>>
    ) {
        val entityBinder = addOrFindEntitySolver(entityVar, solvers, datalog)
        val matches = listPossible(entityBinder.possible, listAll = { datalog.ents().map { (it) } })
            .filter { datalog.isAsserted(it, attr, this.value) }

        solvers[entityVar] = entityBinder.setPossible(Possible.fromList(matches))
    }

    private fun Rule.SlotAttr.squeeze(datalog: Datalist, binders: MutableMap<String, Solver<*>>) {
        val entityBinder = addOrFindEntitySolver(entityVar, binders, datalog)
        val matches = listPossible(entityBinder.possible, listAll = { datalog.ents().map { (it) } })
            .filter { datalog.isAsserted(it, attr) }

        solvers[entityVar] = entityBinder.setPossible(Possible.fromList(matches))
    }

    private fun addOrFindEntitySolver(
        entityVar: String,
        binders: MutableMap<String, Solver<*>>,
        datalog: Datalist
    ): Solver<Long> = binders.addOrFindSolver(
        name = entityVar,
        valueClass = Long::class.java,
        allSolutions = { datalog.ents().map { (it) } }
    )

    private fun addOrFindAttrSolver(
        attrVar: String,
        binders: MutableMap<String, Solver<*>>,
        datalog: Datalist
    ): Solver<Keyword> = binders.addOrFindSolver(
        name = attrVar,
        valueClass = Keyword::class.java,
        allSolutions = { datalog.attrs().map { (it) } }
    )

    private fun addOrFindValueSolver(
        valueVar: String,
        binders: MutableMap<String, Solver<*>>,
        datalog: Datalist
    ): Solver<Any> {
        return binders.addOrFindSolver(
            name = valueVar,
            valueClass = Any::class.java,
            allSolutions = datalog::values
        )
    }

    private fun <T : Any> MutableMap<String, Solver<*>>.addOrFindSolver(
        name: String,
        valueClass: Class<T>,
        allSolutions: () -> Sequence<T>
    ): Solver<T> = this[name]
        ?.let {
            check(it.valueClass == valueClass) {
                "${it.valueClass} failed to match $name's $valueClass"
            }
            @Suppress("UNCHECKED_CAST")
            it as Solver<T>
        }
        ?: Solver(name, valueClass, allSolutions).also {
            this[name] = it
        }

    private fun <T : Any> listValuesInSolver(solver: Solver<T>) =
        listPossible(solver.possible, listAll = { solver.listAll() })

    private fun <T : Any> listPossible(
        possible: Possible<T>,
        listAll: () -> Sequence<T>
    ): List<T> {
        return (if (possible is Possible.All) listAll().toList() else possible.toList()).distinct()
    }
}