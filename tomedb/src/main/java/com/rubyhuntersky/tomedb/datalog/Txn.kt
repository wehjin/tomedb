package com.rubyhuntersky.tomedb.datalog

import java.util.*

internal data class Txn(
    val standing: Standing,
    val time: Date,
    val txnId: TxnId
) {
    val isAsserted: Boolean
        get() = standing == Standing.Asserted
}