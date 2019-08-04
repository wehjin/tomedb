package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword

data class Projection<out T : Any>(val ent: Long, val attr: Keyword, val value: T) {

    fun toEntValue(): EntValue<T> = EntValue(this.ent, this.value)
}