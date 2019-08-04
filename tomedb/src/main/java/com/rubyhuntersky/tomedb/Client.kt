package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.connection.Session
import java.io.File

class Client {
    fun connect(dataDir: File, spec: List<Attribute>? = null): Session {
        return Session(dataDir, spec)
    }

}
