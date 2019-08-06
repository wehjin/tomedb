package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value

data class Solver<out T : Any>(
    val name: String,
    val valueClass: Class<out T>,
    val listAll: () -> Sequence<Value<T>>,
    val possible: Possible<T> = Possible.All
) {
    fun setPossible(possible: Possible<*>): Solver<T> {
        return copy(possible = possible as Possible<T>)
    }

    override fun toString(): String {
        return "Binder(name='$name', solutions=$possible)"
    }
}
