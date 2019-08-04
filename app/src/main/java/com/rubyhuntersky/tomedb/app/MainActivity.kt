package com.rubyhuntersky.tomedb.app

import android.os.Bundle
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
        get() = (this.application as CounterApplication).sessionChan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this@MainActivity.textView.text = getString(R.string.loading)
        launch {
            val counterCount = Counter.Count().firstOrNull()
            val count = counterCount?.valueAsLong()
            val text = count?.let { "$it" } ?: "No counter."
            this@MainActivity.textView.text = text
        }
    }
}
