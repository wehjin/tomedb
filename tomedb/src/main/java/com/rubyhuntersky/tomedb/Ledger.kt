package com.rubyhuntersky.tomedb

import java.util.*

interface Ledger {
    interface Reader {
        val linesUnread: Long
        fun readLine(): Line
    }

    data class Line(
        val entity: Long,
        val attrName: AttrName,
        val value: Value,
        val isAsserted: Boolean,
        val time: Date
    )

    interface Writer {
        fun writeLine(line: Line)
        fun commit()
    }
}