package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

data class Fact(
    val entity: Long,
    val attr: ItemName,
    val value: Value,
    val standing: Standing,
    val inst: Date,
    val txn: TxnId
) {
    sealed class Standing {
        object Asserted : Standing()
        object Retracted : Standing()
    }
}