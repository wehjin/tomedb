package com.rubyhuntersky.tomedb

data class Solver<out T : Any>(
    val name: String,
    val valueClass: Class<out T>,
    val listAll: () -> Sequence<T>,
    val possible: Possible<T> = Possible.All
) {
    fun setPossible(possible: Possible<*>): Solver<T> {
        return copy(possible = possible as Possible<T>)
    }

    override fun toString(): String {
        return "Binder(name='$name', solutions=$possible)"
    }
}
