package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Meter
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

data class Fact(
    val entity: Long,
    val meter: Meter,
    val value: Value,
    val standing: Standing,
    val inst: Date,
    val txn: TxnId
) {
    sealed class Standing : Meter {

        object Asserted : Standing()
        object Retracted : Standing()

        val isAsserted: Boolean
            get() = this == Asserted

        override fun toString(): String = toMeterString()
    }
}