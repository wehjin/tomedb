package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.connection.Connection
import com.rubyhuntersky.tomedb.datascope.UpdateScope
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UpdateScope {

    override val conn: Connection
        get() = (this.application as MainApplication).conn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkoutLatest {
            val count = slot("count")
            val found = find {
                rules = listOf(
                    "counter" has Counter.Count eq count,
                    -"counter" and count
                )
            }
            Log.d(this@MainActivity::class.java.simpleName, "FOUND: $found")
            val counterCount = found(count).firstOrNull()
            this@MainActivity.textView.text = counterCount?.v?.toString() ?: "Counter is missing"
        }
    }
}
