package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.NamedItem
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact

data class FactAction(
    val entity: Long,
    val attr: ItemName,
    val value: Value,
    val type: Type
) {
    sealed class Type : NamedItem {

        object Assert : Type()
        object Retract : Type()

        override fun toString(): String = itemName.toString()

        fun toStanding(): Fact.Standing = when (this) {
            Assert -> Fact.Standing.Asserted
            Retract -> Fact.Standing.Retracted
        }

        companion object {
            fun valueOf(assert: Boolean) = if (assert) Assert else Retract
        }
    }
}