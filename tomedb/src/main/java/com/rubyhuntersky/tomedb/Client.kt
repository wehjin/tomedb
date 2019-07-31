package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.connection.Connection
import com.rubyhuntersky.tomedb.connection.ConnectionStarter

class Client {
    fun connect(starter: ConnectionStarter, writer: Ledger.Writer) =
        Connection(writer, starter)
}
