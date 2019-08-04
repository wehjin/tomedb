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

class CounterApplication : Application(), CoroutineScope, ClientScope {

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override val dbDir: File
        get() = File(filesDir, "tome")

    override val dbSpec: List<Attribute>
        get() = Counter.values().toList()

    lateinit var sessionChan: SessionChannel

    override fun onCreate() {
        super.onCreate()
        sessionChan = clientConnect {
            val counterCount = Counter.Count().firstOrNull()
            if (counterCount == null) {
                Log.i(TAG, "NO COUNTER: Add root instance.")
                transact(updates = setOf(Update(1000, Counter.Count, 33())))
            } else {
                Log.i(TAG, "EXISTING COUNTER: ${counterCount.ent}, COUNT: ${counterCount.valueAsLong()}")
            }
        }
    }

    override fun onTerminate() {
        sessionChan.close()
        super.onTerminate()
    }

    companion object {
        val TAG: String = CounterApplication::class.java.simpleName
    }
}