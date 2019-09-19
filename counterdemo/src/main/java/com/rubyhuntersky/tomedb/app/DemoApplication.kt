package com.rubyhuntersky.tomedb.app

import android.app.Application
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.scopes.client.ClientScope
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import kotlin.coroutines.CoroutineContext

class DemoApplication : Application(), CoroutineScope, ClientScope {

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override val dbDir: File
        get() = File(filesDir, "tome")

    override val dbSpec: List<Attribute>
        get() = Counter.values().toList()

    lateinit var connectionScope: SessionScope

    override fun onCreate() {
        super.onCreate()
        connectionScope = connectToDatabase()
    }

    override fun onTerminate() {
        connectionScope.sessionChannel.close()
        super.onTerminate()
    }

    companion object {
        val TAG: String = DemoApplication::class.java.simpleName
    }
}