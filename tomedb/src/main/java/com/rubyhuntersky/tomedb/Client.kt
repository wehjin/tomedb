package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.connection.Connection
import java.io.File

class Client {
    fun connect(dataDir: File, spec: List<Attribute>? = null) = Connection(dataDir, spec)
}
