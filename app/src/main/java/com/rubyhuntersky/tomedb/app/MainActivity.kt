package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.datascope.ConnectionScope
import java.io.File

class MainActivity : AppCompatActivity(), ConnectionScope {

    override val dbDir: File
        get() = File(filesDir, "tome")

    override val dbSpec: List<Attribute>
        get() = Counter.values().toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connect {
            latest {
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
