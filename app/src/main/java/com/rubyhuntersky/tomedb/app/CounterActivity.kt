package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.basics.Ident
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.ConnectionScope
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext

class CounterActivity : AppCompatActivity(), ConnectionScope, CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Main + job

    override val dbSessionChannel: SessionChannel
        get() = (application as DemoApplication).connectionScope.dbSessionChannel

    sealed class ActorMsg {
        object Incr : ActorMsg()
        object Decr : ActorMsg()
    }

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)
        this@CounterActivity.textView.text = getString(R.string.loading)

        val counterIdent = Ident.of(Counter, 0)
        val actor = actor<ActorMsg> {
            var count: Long = init(counterIdent)
            for (msg in channel) {
                when (msg) {
                    is ActorMsg.Incr -> update(counterIdent, ++count)
                    is ActorMsg.Decr -> update(counterIdent, --count)
                }
            }
        }
        plusButton.setOnClickListener { _ -> actor.offer(ActorMsg.Incr) }
        minusButton.setOnClickListener { _ -> actor.offer(ActorMsg.Decr) }
    }

    private suspend fun init(counterIdent: Ident.Local): Long {
        val count = Counter.Count(counterIdent) as? Long ?: 42
        return count.also { render(it) }
    }

    private suspend fun update(counterIdent: Ident, newCount: Long) {
        dbWriteFact(counterIdent, Counter.Count, newCount)
        render(newCount)
    }

    private fun render(count: Long) {
        this@CounterActivity.textView.text = "$count"
    }
}
