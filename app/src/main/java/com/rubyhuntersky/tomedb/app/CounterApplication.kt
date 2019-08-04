package com.rubyhuntersky.tomedb.app

import android.app.Application
import android.util.Log
import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import kotlin.coroutines.CoroutineContext

class CounterApplication : Application(), ClientScope, CoroutineScope {

    override val dbDir: File
        get() = File(filesDir, "tome")

    override val dbSpec: List<Attribute>
        get() = Counter.values().toList()

    lateinit var conn: SessionChannel

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate() {
        super.onCreate()
        conn = connect {
            checkoutMutable {
                val slot = slot("counter")
                val counter = find { rules = listOf(-slot, slot capture Counter.Count) }().firstOrNull()
                Log.i(this::class.java.simpleName, "COUNTER: $counter")
                if (counter == null) {
                    transact(updates = setOf(Update(1000, Counter.Count, 33())))
                }
            }
        }
    }

    override fun onTerminate() {
        conn.close()
        super.onTerminate()
    }
}