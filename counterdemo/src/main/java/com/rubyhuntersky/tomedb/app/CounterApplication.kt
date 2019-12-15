package com.rubyhuntersky.tomedb.app

import android.app.Application
import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.tomicOf
import java.io.File

class CounterApplication : Application() {

    sealed class Edit {
        data class Count(val count: Long) : Edit()
    }

    override fun onCreate() {
        super.onCreate()
        tomic = tomicOf(dir = File(filesDir, "tome")) {
            on(Edit.Count::class.java) { write(Counter.Count, edit.count) }
            Counter.attrs().toList()
        }
    }

    override fun onTerminate() {
        tomic.close()
        super.onTerminate()
    }

    companion object {
        lateinit var tomic: Tomic<Edit>
    }
}