package com.rubyhuntersky.tomedb

class Client {
    fun connect(writer: Ledger.Writer, reader: Ledger.Reader? = null) = Connection(writer)
}