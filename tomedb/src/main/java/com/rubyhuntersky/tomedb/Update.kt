package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

data class Update(
    val entity: Long,
    val attr: Keyword,
    val value: Value<*>,
    val action: Action = Action.Declare
) {
    sealed class Action : Keyword {
        object Declare : Action()
        object Retract : Action()

        override fun toString(): String = toKeywordString()

        companion object {
            fun valueOf(assert: Boolean) = if (assert) Declare else Retract
        }
    }
}