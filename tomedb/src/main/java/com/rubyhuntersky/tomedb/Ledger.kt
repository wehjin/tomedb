package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Attr
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

interface Ledger {
    interface Reader {
        val linesUnread: Long
        fun readLine(): Line
    }

    data class Line(
        val entity: Long,
        val attr: Attr,
        val value: Value,
        val isAsserted: Boolean,
        val time: Date
    )

    interface Writer {
        fun writeLine(line: Line)
        fun commit()
    }
}