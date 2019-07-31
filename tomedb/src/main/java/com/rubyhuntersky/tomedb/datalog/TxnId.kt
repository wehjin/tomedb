package com.rubyhuntersky.tomedb.datalog

data class TxnId(val height: Long) {

    operator fun inc(): TxnId = TxnId(height + 1)
}