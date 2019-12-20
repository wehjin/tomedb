package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.database.Database

interface MinionMob<A : Attribute2<Ent>> {
    val basis: Database
    val leader: Leader<A>
    val minions: Set<Minion<A>>
    val minionsByEnt: Map<Long, Minion<A>>
    val minionOrNull: Minion<A>?
    val minionList: List<Minion<A>>
    fun minion(ent: Long): Minion<A>
    fun minionOrNull(ent: Long): Minion<A>?
    fun <R> visit(block: MinionMob<A>.() -> R): R
}


interface MinionMobReformScope<A : Attribute2<Ent>> : MinionMob<A> {

    var reforms: List<Form<*>>
    fun formMinion(ent: Long, init: EntReformScope.() -> Unit = {}): List<Form<*>>
}
