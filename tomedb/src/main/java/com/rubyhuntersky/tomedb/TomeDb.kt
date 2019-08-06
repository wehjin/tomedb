package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

sealed class Rule {
    data class SlotAttr(val entityVar: String, val attr: Keyword) : Rule()
    data class SlotAttrValue(val entityVar: String, val value: Value<*>, val attr: Keyword) : Rule()
    data class SlotAttrESlot(val entityVar: String, val entityValueVar: String, val attr: Keyword) : Rule()
    data class SlotAttrSlot(val entityVar: String, val valueVar: String, val attr: Keyword) : Rule()
    data class SlotSlotSlot(val entityVar: String, val attrVar: String, val valueVar: String) : Rule()
}

data class Input<T : Any>(val label: String, val value: Value<T>) {

    internal fun toSolver(): Solver<T> =
        Solver(label, value.valueClass, { listOf(value).asSequence() }, Possible.One(value))
}

sealed class Possible<out T : Any> {

    abstract fun toList(): List<Value<T>>

    object None : Possible<Nothing>() {
        override fun toList(): List<Nothing> = emptyList()
    }

    data class One<T : Any>(val item: Value<T>) : Possible<T>() {
        override fun toList(): List<Value<T>> = listOf(item)
    }

    data class Some<T : Any>(val items: List<Value<T>>) : Possible<T>() {
        override fun toList(): List<Value<T>> = items
    }

    object All : Possible<Nothing>() {
        override fun toList(): List<Nothing> = error("Solutions unknown")
    }

    companion object {
        fun <T : Any> fromList(list: List<Value<T>>): Possible<T> {
            val set = list.toSet()
            return when (set.size) {
                0 -> None
                1 -> One(set.first())
                else -> Some(set.toList())
            }
        }
    }
}

