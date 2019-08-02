package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

data class Update(
    val entity: Long,
    val attr: Keyword,
    val value: Value<*>,
    val type: Type
) {
    sealed class Type : Keyword {
        companion object {
            fun valueOf(assert: Boolean) = if (assert) Assert else Retract
        }

        object Assert : Type()
        object Retract : Type()

        override fun toString(): String = toKeywordString()
    }
}