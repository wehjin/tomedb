package com.rubyhuntersky.tomedb

data class EntValue<out T : Any>(val ent: Long, val value: T) {

    inline fun <reified U : Any> valueAsType(): U? = value as? U
    fun valueAsLong(): Long? = valueAsType()
}