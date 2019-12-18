package com.rubyhuntersky.tomedb.app

import android.app.Application
import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.tomicOf
import java.io.File

class CounterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        tomic = tomicOf(dir = File(filesDir, "tome")) { Counter.attrs().toList() }
    }

    override fun onTerminate() {
        tomic.close()
        super.onTerminate()
    }

    companion object {
        lateinit var tomic: Tomic
    }
}