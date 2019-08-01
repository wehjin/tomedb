package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.connection.Connection
import com.rubyhuntersky.tomedb.connection.ConnectionStarter
import java.nio.file.Path

class Client {
    fun connect(dataDir: Path, starter: ConnectionStarter) = Connection(dataDir, starter)
}
