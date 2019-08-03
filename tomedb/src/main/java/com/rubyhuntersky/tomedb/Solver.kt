package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value

data class Solver<out T : Any>(
    val name: String,
    val valueClass: Class<out T>,
    val allSolutions: () -> List<Value<T>>,
    val solutions: Solutions<T> = Solutions.All
) {
    fun setSolutions(solutions: Solutions<*>): Solver<T> {
        return copy(solutions = solutions as Solutions<T>)
    }

    override fun toString(): String {
        return "Binder(name='$name', solutions=$solutions)"
    }
}
