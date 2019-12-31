package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.findQuantInData
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.database.Database

data class Leader<A : Attribute2<Ent>>(val ent: Long, val attr: A)

interface Minion<A : Attribute2<Ent>> : EntDataHolder {
    val leader: Leader<A>
}

val <A : Attribute2<Ent>> Minion<A>.unform: List<Form<*>>
    get() = this.reform { leader.attr set null }

fun <A : Attribute2<Ent>> Database.getMinions(leader: Leader<A>): Sequence<Minion<A>> {
    val dataPairs = getEntDataPairs(leader.attr.toKeyword())
    val quant = Ent(leader.ent)
    val filteredPairs = dataPairs.filter { (_, data) ->
        findQuantInData(data, leader.attr) == quant
    }
    return filteredPairs.map { (ent, data) ->
        object : Minion<A> {
            override val ent: Long = ent
            override val data: Map<Keyword, Any> = data
            override val leader: Leader<A> = leader
        }
    }
}

fun <A : Attribute2<Ent>> Tomic.minions(leader: Leader<A>): Set<Minion<A>> {
    return visitMinions(leader) { minions }
}

fun <A : Attribute2<Ent>, R> Tomic.visitMinions(
    leader: Leader<A>, block: MinionMob<A>.() -> R
): R = collectMinions(leader).visit(block)

fun <A : Attribute2<Ent>> Tomic.collectMinions(leader: Leader<A>): MinionMob<A> {
    val basis: Database = getDb()
    return object : MinionMob<A> {
        override val basis: Database = basis
        override val leader: Leader<A> = leader
        override val minions: Set<Minion<A>> by lazy { basis.getMinions(leader).toSet() }
        override val minionsByEnt: Map<Long, Minion<A>> by lazy { minions.associateBy(Minion<A>::ent) }
        override val minionOrNull: Minion<A>? by lazy { minions.firstOrNull() }
        override val minionList: List<Minion<A>> by lazy { minions.toList() }
        override fun minion(ent: Long): Minion<A> = checkNotNull(minionsByEnt[ent])
        override fun minionOrNull(ent: Long): Minion<A>? = minionsByEnt[ent]
        override fun <R> visit(block: MinionMob<A>.() -> R): R = run(block)
    }
}

fun <A : Attribute2<Ent>, R> Tomic.reformMinions(
    leader: Leader<A>,
    block: MinionMobReformScope<A>.() -> R
): R {
    var mob = collectMinions(leader)
    val mobReform = object : MinionMobReformScope<A> {
        override val basis: Database get() = mob.basis
        override val leader: Leader<A> get() = mob.leader
        override val minions: Set<Minion<A>> get() = mob.minions
        override val minionsByEnt: Map<Long, Minion<A>> = mob.minionsByEnt
        override val minionOrNull: Minion<A>? = mob.minionOrNull
        override val minionList: List<Minion<A>> = mob.minionList
        override fun minion(ent: Long): Minion<A> = mob.minion(ent)
        override fun minionOrNull(ent: Long): Minion<A>? = mob.minionOrNull(ent)
        override fun <R> visit(block: MinionMob<A>.() -> R): R = mob.visit(block)

        override var reforms: List<Form<*>> = emptyList()
            set(value) {
                check(field.isEmpty())
                field = value.also { write(value) }
                mob = collectMinions(leader)
            }

        override fun formMinion(ent: Long, init: EntReformScope.() -> Unit): List<Form<*>> {
            return reformEnt(ent) {
                leader.attr set Ent(leader.ent)
                this.init()
            }
        }
    }
    return mobReform.run(block)
}
