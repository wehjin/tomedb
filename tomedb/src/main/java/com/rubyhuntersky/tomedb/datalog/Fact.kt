package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.GroupedItem
import com.rubyhuntersky.tomedb.basics.Keyword
import java.util.*

data class Fact(
    val entity: Long,
    val attr: Keyword,
    val value: Any,
    val standing: Standing,
    val inst: Date,
    val txn: TxnId
) {
    sealed class Standing : GroupedItem {

        object Asserted : Standing()
        object Retracted : Standing()

        val isAsserted: Boolean
            get() = this == Asserted

        val isRetracted: Boolean
            get() = !isAsserted

        fun flip(): Standing = if (isAsserted) Retracted else Asserted

        fun asByte(): Byte = when (this) {
            Asserted -> 1
            Retracted -> 0
        }

        override fun toString(): String = toGroupedItemString()

        companion object {
            fun from(byte: Byte): Standing = if (byte.toInt() == 0) Retracted else Asserted
        }
    }
}