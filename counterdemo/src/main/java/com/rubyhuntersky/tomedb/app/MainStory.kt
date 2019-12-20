package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.Tomic
import com.rubyhuntersky.tomedb.reformEnt
import com.rubyhuntersky.tomedb.reformPeers
import com.rubyhuntersky.tomedb.visitPeers

data class CountingMdl(val count: Long)

sealed class CountingMsg {
    object Incr : CountingMsg()
    object Decr : CountingMsg()
}

private const val counterEnt: Long = 123456

fun countingStory(tomic: Tomic): Pair<CountingMdl, (CountingMdl, CountingMsg) -> CountingMdl> {
    val init = CountingMdl(
        count = tomic.visitPeers(Counter.Count2) { peerOrNull?.let { it[Counter.Count2] } ?: 42L }
    )


    fun update(mdl: CountingMdl, msg: CountingMsg): CountingMdl {
        val newCount = when (msg) {
            CountingMsg.Incr -> mdl.count + 1
            CountingMsg.Decr -> mdl.count - 1
        }
        return tomic.reformPeers(Counter.Count2) {
            reforms = reformEnt(counterEnt) { Counter.Count2 set newCount }
            mdl.copy(count = peerOrNull!![Counter.Count2]!!)
        }
    }
    return Pair(init, ::update)
}
