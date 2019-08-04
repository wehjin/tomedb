package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), SessionScope, CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Main + job

    override val sessionChannel: SessionChannel
        get() = (application as CounterApplication).sessionScope.sessionChannel

    sealed class CounterMsg {
        object Incr : CounterMsg()
        object Decr : CounterMsg()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this@MainActivity.textView.text = getString(R.string.loading)

        val counter = actor<CounterMsg> {
            val counterCount = Counter.Count().firstOrNull()
            if (counterCount == null) {
                renderNoCount()
            } else {
                val ent = counterCount.ent
                var count = counterCount.valueAsLong()!!
                renderCount(count)
                for (msg in channel) {
                    when (msg) {
                        is CounterMsg.Incr -> updateRenderCount(ent, ++count)
                        is CounterMsg.Decr -> updateRenderCount(ent, --count)
                    }
                }
            }
        }
        plusButton.setOnClickListener { _ -> counter.offer(CounterMsg.Incr) }
        minusButton.setOnClickListener { _ -> counter.offer(CounterMsg.Decr) }
    }

    private suspend fun updateRenderCount(counter: Long, count: Long) {
        assertFact(counter, Counter.Count, count)
        renderCount(count)
    }

    private fun renderCount(count: Long) {
        this@MainActivity.textView.text = "$count"
    }

    private fun renderNoCount() {
        this@MainActivity.textView.text = getString(R.string.no_counter)
    }
}
