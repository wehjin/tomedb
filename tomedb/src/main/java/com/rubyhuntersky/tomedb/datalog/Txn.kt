package com.rubyhuntersky.tomedb.datalog

import java.util.*

internal data class Txn(
    val standing: Fact.Standing,
    val time: Date,
    val txnId: TxnId
) {
    val isAsserted: Boolean
        get() = standing == Fact.Standing.Asserted
}