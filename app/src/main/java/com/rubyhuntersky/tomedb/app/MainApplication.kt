package com.rubyhuntersky.tomedb.app

import android.app.Application
import android.util.Log
import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.connection.Connection
import com.rubyhuntersky.tomedb.datascope.ConnectionScope
import java.io.File

class MainApplication : Application(), ConnectionScope {

    override val dbDir: File
        get() = File(filesDir, "tome")

    override val dbSpec: List<Attribute>
        get() = Counter.values().toList()

    lateinit var conn: Connection

    override fun onCreate() {
        super.onCreate()
        conn = connect {
            checkoutLatest {
                val counter = slot("counter")
                    .let {
                        val result = find { rules = listOf(-it, it capture Counter.Count) }
                        it(result)
                    }
                    .firstOrNull()
                Log.i(this::class.java.simpleName, "COUNTER: $counter")
                if (counter == null) {
                    transact(updates = setOf(Update(1000, Counter.Count, 33())))
                }
            }
        }
    }
}