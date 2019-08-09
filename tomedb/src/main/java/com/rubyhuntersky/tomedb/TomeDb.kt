package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword

sealed class Rule {
    data class SlotAttr(val entityVar: String, val attr: Keyword) : Rule()
    data class SlotAttrValue(val entityVar: String, val value: Any, val attr: Keyword) : Rule()
    data class SlotAttrESlot(val entityVar: String, val entityValueVar: String, val attr: Keyword) : Rule()
    data class SlotAttrSlot(val entityVar: String, val valueVar: String, val attr: Keyword) : Rule()
    data class SlotSlotSlot(val entityVar: String, val attrVar: String, val valueVar: String) : Rule()
}

data class Input<T : Any>(val label: String, val value: T) {

    internal fun toSolver(): Solver<T> {
        val valueClass = value::class.javaPrimitiveType ?: value::class.javaObjectType
        return Solver(label, valueClass, { listOf(value).asSequence() }, Possible.One(value))
    }
}

sealed class Possible<out T : Any> {

    abstract fun toList(): List<T>

    object None : Possible<Nothing>() {
        override fun toList(): List<Nothing> = emptyList()
    }

    data class One<T : Any>(val item: T) : Possible<T>() {
        override fun toList(): List<T> = listOf(item)
    }

    data class Some<T : Any>(val items: List<T>) : Possible<T>() {
        override fun toList(): List<T> = items
    }

    object All : Possible<Nothing>() {
        override fun toList(): List<Nothing> = error("Solutions unknown")
    }

    companion object {
        fun <T : Any> fromList(list: List<T>): Possible<T> {
            val set = list.toSet()
            return when (set.size) {
                0 -> None
                1 -> One(set.first())
                else -> Some(set.toList())
            }
        }
    }
}

