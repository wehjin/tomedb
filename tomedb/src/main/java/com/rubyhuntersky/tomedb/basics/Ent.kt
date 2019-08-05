package com.rubyhuntersky.tomedb.basics

import kotlin.math.absoluteValue

data class Ent(val long: Long) {
    init {
        require(long == long.absoluteValue)
    }

    fun mix(ent: Ent): Ent = Ent((31 * this.long + ent.long).absoluteValue)
}