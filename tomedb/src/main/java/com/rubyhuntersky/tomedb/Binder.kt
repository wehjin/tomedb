package com.rubyhuntersky.tomedb

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
