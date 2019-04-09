package com.rubyhuntersky.tomedb

class TransientLedgerWriter : Ledger.Writer {
    val lines = mutableListOf<Ledger.Line>()

    override fun writeLine(line: Ledger.Line) {
        lines.add(line)
    }

    override fun commit() = Unit
}