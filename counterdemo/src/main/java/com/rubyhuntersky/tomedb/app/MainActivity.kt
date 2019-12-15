package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var job = Job()
    override val coroutineContext = Main + job

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        val actor = actor<CountingMsg> {
            val (init, next) = countingStory(CounterApplication.tomic)
            var latest = init.also { render(it) }
            for (change in channel) {
                latest = next(latest, change).also { render(it) }
            }
        }
        plusButton.setOnClickListener { _ -> actor.offer(CountingMsg.Incr) }
        minusButton.setOnClickListener { _ -> actor.offer(CountingMsg.Decr) }
    }

    private fun render(mdl: CountingMdl) {
        this@MainActivity.textView.text = "${mdl.count}"
    }
}
