package com.rubyhuntersky.tomedb.minion

import com.rubyhuntersky.tomedb.EntDataHolder
import com.rubyhuntersky.tomedb.Form
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.get
import com.rubyhuntersky.tomedb.reform

data class Leader<A : Attribute2<Ent>>(val ent: Long, val attr: A)

interface Minion<A : Attribute2<Ent>> : EntDataHolder {
    val leader: Leader<A>
}

fun <A : Attribute2<Ent>> unform(minion: Minion<A>): List<Form<*>> {
    return minion.reform { minion.leader.attr set null }
}

fun <A : Attribute2<Ent>> Minion<A>.hasLeader(leader: Leader<A>): Boolean {
    return this[leader.attr]?.number == leader.ent
}
