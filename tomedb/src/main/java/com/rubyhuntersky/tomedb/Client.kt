package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.connection.Connection
import java.nio.file.Path

class Client {
    fun connect(dataDir: Path, spec: List<Attribute>? = null) = Connection(dataDir, spec)
}
