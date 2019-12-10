package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext

class CounterActivity : AppCompatActivity(), CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Main + job


    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        val actor = actor<CountingMsg> {
            val (init, next) = countingStory(DemoApplication.session)
            var latest = init.also { render(it) }
            for (change in channel) {
                latest = next(latest, change).also { render(it) }
            }
        }
        plusButton.setOnClickListener { _ -> actor.offer(CountingMsg.Incr) }
        minusButton.setOnClickListener { _ -> actor.offer(CountingMsg.Decr) }
    }

    private fun render(mdl: CountingMdl) {
        this@CounterActivity.textView.text = "${mdl.count}"
    }
}
