package com.rubyhuntersky.tomedb.datalog

import java.util.*

internal data class Txn(val time: Date, val standing: Fact.Standing, val txnId: TxnId) {
    val isAsserted: Boolean
        get() = standing == Fact.Standing.Asserted
}