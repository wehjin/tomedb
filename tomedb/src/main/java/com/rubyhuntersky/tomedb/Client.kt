package com.rubyhuntersky.tomedb

class Client {
    fun connect(starter: ConnectionStarter, writer: Ledger.Writer) = Connection(writer, starter)
}
