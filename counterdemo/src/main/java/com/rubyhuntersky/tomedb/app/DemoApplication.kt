package com.rubyhuntersky.tomedb.app

import android.app.Application
import com.rubyhuntersky.tomedb.data.launchSession
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import com.rubyhuntersky.tomedb.scopes.session.cancel
import java.io.File

class DemoApplication : Application() {

    lateinit var session: SessionScope

    override fun onCreate() {
        super.onCreate()
        session = launchSession(File(filesDir, "tome"), Counter.attrs().toList())
    }

    override fun onTerminate() {
        session.cancel()
        super.onTerminate()
    }
}