package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import kotlin.math.absoluteValue
import kotlin.random.Random

sealed class Mod<T> {

    abstract val ent: Long
    abstract val attribute: Attribute2<T>
    abstract fun quantAsScript(): String

    data class Set<T : Any>(
        override val ent: Long,
        override val attribute: Attribute2<T>,
        val quant: T
    ) : Mod<T>() {
        override fun quantAsScript(): String = attribute.scriber.scribe(quant)
    }

    data class Clear<T : Any>(
        override val ent: Long,
        override val attribute: Attribute2<T>
    ) : Mod<T>() {
        override fun quantAsScript(): String = attribute.scriber.emptyScript
    }
}

fun modEnt(
    ent: Long = Random.nextLong().absoluteValue,
    init: EntModScope.() -> Unit
): List<Mod<*>> {
    return mutableListOf<Mod<*>>().also { mods ->
        object : EntModScope {
            override fun <T : Any> bind(attribute: Attribute2<T>, quant: T?) {
                val mod = when (quant) {
                    null -> Mod.Clear(ent, attribute)
                    else -> Mod.Set(ent, attribute, quant)
                }
                mods.add(mod)
            }

            override infix fun <T : Any> Attribute2<T>.set(quant: T?) = bind(this, quant)
        }.init()
    }
}

interface EntModScope {
    fun <T : Any> bind(attribute: Attribute2<T>, quant: T?)
    infix fun <T : Any> Attribute2<T>.set(quant: T?)
}

