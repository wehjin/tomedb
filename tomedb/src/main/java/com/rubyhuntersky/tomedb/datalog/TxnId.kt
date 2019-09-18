package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.basics.longFromBytes

data class TxnId(val height: Long) {

    operator fun inc(): TxnId = TxnId(height + 1)
    fun toBytes(): ByteArray = bytesFromLong(height)

    companion object {
        const val bytesLen: Int = Long.SIZE_BYTES
        fun from(bytes: ByteArray): TxnId = TxnId(longFromBytes(bytes))
    }
}