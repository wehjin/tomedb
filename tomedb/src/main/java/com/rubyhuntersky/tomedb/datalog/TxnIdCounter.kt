package com.rubyhuntersky.tomedb.datalog

import java.io.File
import java.io.FileNotFoundException

internal class TxnIdCounter(folder: File) {

    private val file = File(folder, "next-txn-id")

    private var nextHeight: Long = file.let {
        try {
            val values = mutableListOf<Long>()
            file.forEachLine {
                val long = it.toLong()
                values.add(long)
            }
            values.first()
        } catch (e: FileNotFoundException) {
            1
        }
    }

    fun nextTxnId(): TxnId = TxnId(nextHeight).also {
        nextHeight++
        file.writeText("$nextHeight\n")
    }
}