package com.rubyhuntersky.tomedb.basics

import kotlin.math.absoluteValue

data class Ent(val number: Long) {
    init {
        require(number == number.absoluteValue)
    }

    fun mix(ent: Ent): Ent = Ent((31 * this.number + ent.number).absoluteValue)
}