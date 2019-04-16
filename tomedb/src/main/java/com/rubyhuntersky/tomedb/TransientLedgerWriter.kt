package com.rubyhuntersky.tomedb

class TransientLedgerWriter : Ledger.Writer {
    val lines = mutableListOf<Ledger.Line>()

    override fun writeLine(line: Ledger.Line) {
        lines.add(line)
    }

    override fun commit() = Unit

    fun toReader(): Ledger.Reader = object : Ledger.Reader {
        private val lines = this@TransientLedgerWriter.lines.toMutableList()

        override val linesUnread: Long
            get() = lines.size.toLong()

        override fun readLine(): Ledger.Line {
            return lines.removeAt(0)
        }
    }
}