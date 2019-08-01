package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.NamedItem
import com.rubyhuntersky.tomedb.basics.Value

data class Update(
    val entity: Long,
    val attr: ItemName,
    val value: Value,
    val type: Type
) {
    sealed class Type : NamedItem {
        companion object {
            fun valueOf(assert: Boolean) = if (assert) Assert else Retract
        }

        object Assert : Type()
        object Retract : Type()

        override fun toString(): String = itemName.toString()
    }
}