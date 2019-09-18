package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.data.Page
import com.rubyhuntersky.tomedb.data.PageSubject
import com.rubyhuntersky.tomedb.data.invoke
import com.rubyhuntersky.tomedb.data.lineOf
import com.rubyhuntersky.tomedb.scopes.session.ConnectionScope
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
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


    data class Mdl(val page: Page<Ent>)

    sealed class Msg {
        object Incr : Msg()
        object Decr : Msg()
    }

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)
        this@CounterActivity.textView.text = getString(R.string.loading)
        val actor = actor<Msg> {
            var mdl = init().also { render(it) }
            for (msg in channel) {
                mdl = update(mdl, msg).also { render(it) }
            }
        }
        plusButton.setOnClickListener { _ -> actor.offer(Msg.Incr) }
        minusButton.setOnClickListener { _ -> actor.offer(Msg.Decr) }
    }

    private suspend fun init(): Mdl {
        val page = dbRead(PageSubject.Entity(Ent.of(Counter, 0)))
        val init = page.plusDefault(Counter.Count, 42L)
        return Mdl(init)
    }

    private suspend fun update(mdl: Mdl, msg: Msg): Mdl {
        val oldCount = mdl.page<Long>(Counter.Count)
        val newPage = when (msg) {
            Msg.Incr -> dbWrite(mdl.page, lineOf(Counter.Count, oldCount + 1L))
            Msg.Decr -> dbWrite(mdl.page, lineOf(Counter.Count, oldCount - 1L))
        }
        return Mdl(newPage)
    }

    private fun render(mdl: Mdl) {
        val count = mdl.page[Counter.Count]
        this@CounterActivity.textView.text = "$count"
    }
}
