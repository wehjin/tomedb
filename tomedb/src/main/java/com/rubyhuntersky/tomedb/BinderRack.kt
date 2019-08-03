package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.AnyValue
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.Value.LONG
import com.rubyhuntersky.tomedb.basics.ValueType
import com.rubyhuntersky.tomedb.datalog.Datalog

class BinderRack(initSolvers: List<Solver<*>>?) {

    private val solvers = initSolvers?.associateBy { it.name }?.toMutableMap() ?: mutableMapOf()

    fun stir(outputs: List<String>, rules: List<Rule>, datalog: Datalog): List<Map<String, Value<*>>> {
        shake(rules, datalog, solvers)
        println("SOLVERS after shake: $solvers")
        val outputSolvers = outputs.map { solvers[it] ?: error("No binder for output $it") }
        val outputBindings = outputSolvers.join(emptyList())

        val checkedOutputBindings = outputBindings
            .filter { outputBinding ->
                val constrainedBinders = solvers.constrainSolutions(outputBinding)
                shake(rules, datalog, constrainedBinders)
                val hasSingleSolutionPerOutput = outputBinding.map { (name, _) -> name }
                    .fold(true) { allPreviousBindersHaveOneSolution, name ->
                        allPreviousBindersHaveOneSolution && constrainedBinders[name]!!.solutions is Solutions.One
                    }
                hasSingleSolutionPerOutput
            }

        return checkedOutputBindings.map { bindings ->
            bindings.associate { (name, value) ->
                val unwrappedValue = if (value is Value.VALUE) value.v.value else value
                Pair(name, unwrappedValue)
            }
        }
    }

    private fun MutableMap<String, Solver<*>>.constrainSolutions(bindings: List<Pair<String, Value<*>>>): MutableMap<String, Solver<*>> {
        return mapValues { (_, binder) -> binder.copy() }
            .also {
                bindings.forEach { (name, value) ->
                    val binder = it[name] ?: error("No binder with name $name.")
                    binder.acceptSolutions(value.toSolutionsOne())
                }
            }.toMutableMap()
    }

    private fun Value<*>.toSolutionsOne(): Solutions.One<Any> =
        when (this) {
            is Value.VALUE -> Solutions.One(v.value)
            else -> Solutions.One(v)
        }

    private tailrec fun List<Solver<*>>.join(prefixes: List<List<Pair<String, Value<*>>>>): List<List<Pair<String, Value<*>>>> {
        return if (this.isEmpty()) prefixes
        else {
            val firstSolver = this[0]
            val valuesInFirstSolver = listValuesInSolver(firstSolver)
            val firstSolverNameAndValues = valuesInFirstSolver.map {
                Pair<String, Value<*>>(firstSolver.name, it)
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

    private fun shake(rules: List<Rule>, datalog: Datalog, binders: MutableMap<String, Solver<*>>) {
        rules.forEach {
            when (it) {
                is Rule.EntityContainsAttr -> it.shake(datalog, binders)
                is Rule.EntityContainsExactValueAtAttr -> it.shake(datalog, binders)
                is Rule.EntityContainsAnyEntityAtAttr -> it.shake(datalog, binders)
                is Rule.EntityContainsAnyValueAtAttr -> it.shake(datalog, binders)
            }
        }
    }

    private fun Rule.EntityContainsAnyValueAtAttr.shake(datalog: Datalog, binders: MutableMap<String, Solver<*>>) {
        val entityBinder = binders.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = datalog::allEntities
        )
        val valueBinder = binders.addSolver(
            name = valueVar,
            valueClass = ValueType.VALUE.toValueClass(),
            allSolutions = datalog::allAssertedValues
        )

        val substitutions = listValuesInSolution(
            solutions = entityBinder.solutions,
            allValues = { datalog.allEntities }
        ).map { entity ->
            listValuesInSolution(
                solutions = valueBinder.solutions,
                allValues = {
                    datalog.entityAttrValues(entity, attr)
                }
            ).map { value -> Pair(entity, value) }
        }.flatten()
            .filter { (entity, value) ->
                datalog.isEntityAttrValueAsserted(entity, attr, value)
            }

        entityBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Value<*>>::first))
        valueBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Value<*>>::second))
    }

    private fun Rule.EntityContainsAnyEntityAtAttr.shake(
        datalog: Datalog,
        binders: MutableMap<String, Solver<*>>
    ) {
        val startBinder = binders.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = datalog::allEntities
        )
        val endBinder = binders.addSolver(
            name = entityValueVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = datalog::allEntities
        )
        val substitutions =
            listValuesInSolution(
                solutions = startBinder.solutions,
                allValues = { datalog.allEntities }
            ).map { start: Long ->
                listValuesInSolution(
                    solutions = endBinder.solutions,
                    allValues = { datalog.allEntities }
                ).map { end: Long -> Pair(start, end) }
            }.flatten()
                .filter {
                    datalog.isEntityAttrValueAsserted(it.first, attr, LONG(it.second))
                }

        startBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Long>::first))
        endBinder.solutions = Solutions.fromList(substitutions.map(Pair<Long, Long>::second))
    }

    private fun Rule.EntityContainsExactValueAtAttr.shake(datalog: Datalog, solvers: MutableMap<String, Solver<*>>) {
        val entityBinder = solvers.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = datalog::allEntities
        )
        val matches = listValuesInSolution(
            solutions = entityBinder.solutions,
            allValues = { datalog.allEntities }
        ).filter {
            datalog.isEntityAttrValueAsserted(it, attr, this.value)
        }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun Rule.EntityContainsAttr.shake(datalog: Datalog, binders: MutableMap<String, Solver<*>>) {
        val entityBinder = binders.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = datalog::allEntities
        )
        val matches = listValuesInSolution(
            solutions = entityBinder.solutions,
            allValues = { datalog.allEntities }
        ).filter {
            datalog.isEntityAttrAsserted(it, attr)
        }
        entityBinder.solutions = Solutions.fromList(matches)
    }

    private fun <T : Any> MutableMap<String, Solver<*>>.addSolver(
        name: String,
        valueClass: Class<T>,
        allSolutions: () -> List<T>
    ): Solver<T> = this[name]
        ?.let {
            check(it.valueClass == valueClass)
            @Suppress("UNCHECKED_CAST")
            it as Solver<T>
        }
        ?: Solver(name, valueClass, allSolutions).also {
            this[name] = it
        }

    private fun <T : Any> listValuesInSolver(solver: Solver<T>) =
        listValuesInSolution(solver.solutions, allValues = { solver.allSolutions.invoke() })
            .map {
                if (it is Value<*>) Value.of(AnyValue(it))
                else Value.of(it)
            }

    private fun <T> listValuesInSolution(solutions: Solutions<T>, allValues: () -> List<T>): List<T> =
        (if (solutions is Solutions.Any) allValues.invoke()
        else solutions.toList()).toSet().toList()
}