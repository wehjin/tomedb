package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.connection.FileSession
import java.io.File

class Client {
    fun connect(dataDir: File, spec: List<Attribute>? = null): FileSession {
        return FileSession(dataDir, spec)
    }

}
