package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import java.util.*

data class Fact(
    val entity: Long,
    val attr: Keyword,
    val quant: Any,
    val standing: Standing,
    val inst: Date,
    val txn: TxnId
)