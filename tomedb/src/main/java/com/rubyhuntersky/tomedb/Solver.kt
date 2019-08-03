package com.rubyhuntersky.tomedb

data class Solver<T : Any>(
    val name: String,
    val valueClass: Class<T>,
    val allSolutions: () -> List<T>,
    var solutions: Solutions<T> = Solutions.Any()
) {

    fun <U : Any> acceptSolutions(solutions: Solutions.One<U>) {
        this.solutions = solutions as Solutions.One<T>
    }

    override fun toString(): String {
        return "Binder(name='$name', solutions=$solutions)"
    }
}
