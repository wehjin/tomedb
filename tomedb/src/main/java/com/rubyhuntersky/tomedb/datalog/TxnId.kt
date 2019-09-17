package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.bytesFromLong

data class TxnId(val height: Long) {

    operator fun inc(): TxnId = TxnId(height + 1)
    fun toBytes(): ByteArray = bytesFromLong(height)
}