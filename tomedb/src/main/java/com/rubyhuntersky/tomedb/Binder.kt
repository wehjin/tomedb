package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value

data class Binder<T : Any>(
    val name: String,
    val allSolutions: () -> List<T>,
    val toValue: (T) -> Value<*>,
    var solutions: Solutions<T> = Solutions.Any()
) {
    fun toValueList(): List<Value<*>> = toList().map { toValue(it) }

    fun <U : Any> acceptSolutions(solutions: Solutions.One<U>) {
        this.solutions = solutions as Solutions.One<T>
    }

    private fun toList(): List<T> = solutions.toList { allSolutions.invoke() }

    override fun toString(): String {
        return "Binder(name='$name', solutions=$solutions)"
    }
}
