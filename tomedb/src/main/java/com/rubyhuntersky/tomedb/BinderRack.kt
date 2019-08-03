package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value
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
                        allPreviousBindersHaveOneSolution && constrainedBinders[name]!!.solutions is Solutions.One<*>
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
        return mapValues { (_, binder) -> binder.copy() }.toMutableMap()
            .also {
                bindings.forEach { (name, value) ->
                    val solver = it[name] ?: error("No binder with name $name.")
                    it[name] = solver.setSolutions(Solutions.One(value))
                }
            }
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
            allSolutions = { datalog.allEntities.map { Value.of(it) } }
        )
        val valueBinder = binders.addSolver(
            name = valueVar,
            valueClass = ValueType.VALUE.toValueClass(),
            allSolutions = datalog::allAssertedValues
        )

        val substitutions = listValuesInSolution(
            solutions = entityBinder.solutions,
            allValues = { datalog.allEntities.map { Value.of(it) } }
        ).map { entity ->
            listValuesInSolution(
                solutions = valueBinder.solutions,
                allValues = {
                    datalog.entityAttrValues(entity.v, attr)
                }
            ).map { value -> Pair(entity, value) }
        }.flatten()
            .filter { (entity, value) ->
                datalog.isEntityAttrValueAsserted(entity.v, attr, value)
            }

        binders[entityVar] =
            entityBinder.setSolutions(Solutions.fromList(substitutions.map(Pair<Value<Long>, Value<*>>::first)))
        binders[valueVar] =
            valueBinder.setSolutions(Solutions.fromList(substitutions.map(Pair<Value<Long>, Value<*>>::second)))
    }

    private fun Rule.EntityContainsAnyEntityAtAttr.shake(
        datalog: Datalog,
        binders: MutableMap<String, Solver<*>>
    ) {
        val startBinder = binders.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = { datalog.allEntities.map { Value.of(it) } }
        )
        val endBinder = binders.addSolver(
            name = entityValueVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = { datalog.allEntities.map { Value.of(it) } }
        )
        val substitutions =
            listValuesInSolution(
                solutions = startBinder.solutions,
                allValues = { datalog.allEntities.map { Value.of(it) } }
            ).map { start: Value<Long> ->
                listValuesInSolution(
                    solutions = endBinder.solutions,
                    allValues = { datalog.allEntities.map { Value.of(it) } }
                ).map { end: Value<Long> -> Pair(start, end) }
            }.flatten()
                .filter {
                    datalog.isEntityAttrValueAsserted(it.first.v, attr, it.second)
                }

        binders[entityVar] =
            startBinder.setSolutions(Solutions.fromList(substitutions.map(Pair<Value<Long>, Value<*>>::first)))
        binders[entityValueVar] =
            endBinder.setSolutions(Solutions.fromList(substitutions.map(Pair<Value<Long>, Value<*>>::second)))
    }

    private fun Rule.EntityContainsExactValueAtAttr.shake(datalog: Datalog, solvers: MutableMap<String, Solver<*>>) {
        val entityBinder = solvers.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = { datalog.allEntities.map { Value.of(it) } }
        )
        val matches = listValuesInSolution(
            solutions = entityBinder.solutions,
            allValues = { datalog.allEntities.map { Value.of(it) } }
        ).filter {
            datalog.isEntityAttrValueAsserted(it.v, attr, this.value)
        }
        solvers[entityVar] = entityBinder.setSolutions(Solutions.fromList(matches))
    }

    private fun Rule.EntityContainsAttr.shake(datalog: Datalog, binders: MutableMap<String, Solver<*>>) {
        val entityBinder = binders.addSolver(
            name = entityVar,
            valueClass = ValueType.LONG.toValueClass(),
            allSolutions = { datalog.allEntities.map { Value.of(it) } }
        )
        val matches = listValuesInSolution(
            solutions = entityBinder.solutions,
            allValues = { datalog.allEntities.map { Value.of(it) } }
        ).filter {
            datalog.isEntityAttrAsserted(it.v, attr)
        }
        solvers[entityVar] = entityBinder.setSolutions(Solutions.fromList(matches))
    }

    private fun <T : Any> MutableMap<String, Solver<*>>.addSolver(
        name: String,
        valueClass: Class<T>,
        allSolutions: () -> List<Value<T>>
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

    private fun <T : Any> listValuesInSolution(
        solutions: Solutions<T>,
        allValues: () -> List<Value<T>>
    ): List<Value<T>> {
        return (if (solutions is Solutions.All) allValues.invoke() else solutions.toList()).distinct()
    }
}