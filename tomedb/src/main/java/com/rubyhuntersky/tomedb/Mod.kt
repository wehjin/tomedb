package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2

sealed class Mod<T> {
    data class Set<T : Any>(
        val ent: Long,
        val attribute: Attribute2<T>,
        val value: T
    ) : Mod<T>() {
        fun asScription(): String = attribute.scriber.scribe(value)
    }
}

fun modEnt(ent: Long, init: EntModScope.() -> Unit): List<Mod<*>> {
    return mutableListOf<Mod<*>>().also { mods ->
        object : EntModScope {
            override fun <T : Any> bind(attribute: Attribute2<T>, quant: T) {
                mods.add(Mod.Set(ent, attribute, quant))
            }

            override infix fun <T : Any> Attribute2<T>.set(quant: T) {
                bind(this, quant)
            }
        }.init()
    }
}

interface EntModScope {
    fun <T : Any> bind(attribute: Attribute2<T>, quant: T)
    infix fun <T : Any> Attribute2<T>.set(quant: T)
}

