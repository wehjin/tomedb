package com.rubyhuntersky.tomedb.app

import android.app.Application
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.scopes.session.Session
import java.io.File

class DemoApplication : Application() {

    lateinit var session: Session

    override fun onCreate() {
        super.onCreate()
        session = startSession(File(filesDir, "tome"), Counter.attrs().toList())
    }

    override fun onTerminate() {
        session.close()
        super.onTerminate()
    }
}