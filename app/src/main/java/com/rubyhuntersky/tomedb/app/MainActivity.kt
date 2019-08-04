package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), SessionScope, CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Main + job

    override val session: SessionChannel
        get() = (this.application as CounterApplication).conn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this@MainActivity.textView.text = getString(R.string.loading)
        launch {
            checkoutMutable {
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
}
