package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Meter
import com.rubyhuntersky.tomedb.basics.Value

data class Update(
    val entity: Long,
    val meter: Meter,
    val value: Value,
    val type: Type
) {
    sealed class Type : Meter {
        companion object {
            fun valueOf(assert: Boolean) = if (assert) Assert else Retract
        }

        object Assert : Type()
        object Retract : Type()

        override fun toString(): String = toMeterString()
    }
}