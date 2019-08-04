package com.rubyhuntersky.tomedb.app

import android.app.Application
import android.util.Log
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    lateinit var sessionScope: SessionScope

    override fun onCreate() {
        super.onCreate()
        sessionScope = clientConnect()
        launch {
            sessionScope.withLiveDb {
                val counterCount = Counter.Count().firstOrNull()
                if (counterCount == null) {
                    Log.i(TAG, "NO COUNTER: Add root instance.")
                    transact(updates = setOf(Update(1000, Counter.Count, 33())))
                } else {
                    Log.i(TAG, "EXISTING COUNTER: ${counterCount.ent}, COUNT: ${counterCount.valueAsLong()}")
                }
            }
        }
    }

    override fun onTerminate() {
        sessionScope.sessionChannel.close()
        super.onTerminate()
    }

    companion object {
        val TAG: String = CounterApplication::class.java.simpleName
    }
}