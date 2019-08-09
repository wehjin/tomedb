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

        override fun toString(): String = toGroupedItemString()
    }
}