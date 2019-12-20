package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import kotlin.math.absoluteValue
import kotlin.random.Random

sealed class Form<T> {

    abstract val ent: Long
    abstract val attribute: Attribute2<T>
    abstract fun quantAsScript(): String

    data class Set<T : Any>(
        override val ent: Long,
        override val attribute: Attribute2<T>,
        val quant: T
    ) : Form<T>() {
        override fun quantAsScript(): String = attribute.scriber.scribe(quant)
    }

    data class Clear<T : Any>(
        override val ent: Long,
        override val attribute: Attribute2<T>
    ) : Form<T>() {
        override fun quantAsScript(): String = attribute.scriber.emptyScript
    }
}

fun reformEnt(
    ent: Long = Random.nextLong().absoluteValue,
    init: EntReformScope.() -> Unit
): List<Form<*>> {
    return mutableListOf<Form<*>>().also { reforms ->
        object : EntReformScope {
            override fun <T : Any> bind(attribute: Attribute2<T>, quant: T?) {
                val reform = when (quant) {
                    null -> Form.Clear(ent, attribute)
                    else -> Form.Set(ent, attribute, quant)
                }
                reforms.add(reform)
            }

            override infix fun <T : Any> Attribute2<T>.set(quant: T?) = bind(this, quant)
        }.init()
    }
}

interface EntReformScope {
    fun <T : Any> bind(attribute: Attribute2<T>, quant: T?)
    infix fun <T : Any> Attribute2<T>.set(quant: T?)
}

