package com.rubyhuntersky.tomedb.app

import android.app.Application
import com.rubyhuntersky.tomedb.data.tomeConnect
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import java.io.File

class DemoApplication : Application() {

    lateinit var session: SessionScope

    override fun onCreate() {
        super.onCreate()
        session = tomeConnect(File(filesDir, "tome"), Counter.attrs().toList())
    }

    override fun onTerminate() {
        session.sessionChannel.close()
        super.onTerminate()
    }
}