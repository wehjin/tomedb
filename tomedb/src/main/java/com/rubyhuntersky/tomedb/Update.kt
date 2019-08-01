package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Attr
import com.rubyhuntersky.tomedb.basics.Value

data class Update(
    val entity: Long,
    val attr: Attr,
    val value: Value,
    val type: Type
) {
    sealed class Type : Attr {
        companion object {
            fun valueOf(assert: Boolean) = if (assert) Assert else Retract
        }

        object Assert : Type()
        object Retract : Type()

        override fun toString(): String = toAttrString()
    }
}