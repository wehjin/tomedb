package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.app.CountingStory.Mdl
import com.rubyhuntersky.tomedb.app.CountingStory.Msg
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

        val actor = actor<Msg> {
            val story = CountingStory(application as DemoApplication)
            var mdl = story.init().also { render(it) }
            for (msg in channel) {
                mdl = story.update(mdl, msg).also { render(it) }
            }
        }
        plusButton.setOnClickListener { _ -> actor.offer(Msg.Incr) }
        minusButton.setOnClickListener { _ -> actor.offer(Msg.Decr) }
    }

    private fun render(mdl: Mdl) {
        this@CounterActivity.textView.text = "${mdl.count}"
    }
}
