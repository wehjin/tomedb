package com.rubyhuntersky.tomedb.app

import android.os.Bundle
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
            val results = find {
                rules = listOf(
                    "counter" capture Counter.Count eq "count",
                    -"counter" and "count"
                )
            }
            val count = slot("count")(results).firstOrNull()
            this@MainActivity.textView.text = count?.v?.toString() ?: "Counter is missing"
        }
    }
}
