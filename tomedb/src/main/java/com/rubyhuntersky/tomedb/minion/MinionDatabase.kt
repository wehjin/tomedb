package com.rubyhuntersky.tomedb.minion

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.findQuantInData
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.get

inline operator fun <A : Attribute2<Ent>, reified T : Any> Database.get(
    leader: Leader<A>,
    ent: Long,
    attr: Attribute2<T>
): T? = minionOrNull(leader, ent)?.get(attr)

operator fun <A : Attribute2<Ent>> Database.get(
    leader: Leader<A>,
    ent: Long
) = minionOrNull(leader, ent)

fun <A : Attribute2<Ent>> Database.minionOrNull(
    leader: Leader<A>,
    ent: Long
): Minion<A>? = visitMinions(leader) { minionOrNull(ent) }

fun <A : Attribute2<Ent>> Database.minions(
    leader: Leader<A>
) = visitMinions(leader) { minions }

fun <A : Attribute2<Ent>, R> Database.visitMinions(
    leader: Leader<A>,
    block: MinionMob<A>.() -> R
): R = collectMinions(leader).visit(block)

fun <A : Attribute2<Ent>> Database.collectMinions(leader: Leader<A>): MinionMob<A> {
    return object : MinionMob<A> {
        override val basis: Database = this@collectMinions
        override val leader: Leader<A> = leader
        override val minions: Set<Minion<A>> by lazy { getMinions(leader).toSet() }
        override val minionsByEnt: Map<Long, Minion<A>> by lazy {
            minions.associateBy(
                Minion<A>::ent
            )
        }
        override val minionOrNull: Minion<A>? by lazy { minions.firstOrNull() }
        override val minionList: List<Minion<A>> by lazy { minions.toList() }
        override fun minion(ent: Long): Minion<A> = checkNotNull(minionsByEnt[ent])
        override fun minionOrNull(ent: Long): Minion<A>? = minionsByEnt[ent]
        override fun <R> visit(block: MinionMob<A>.() -> R): R = run(block)
    }
}

private fun <A : Attribute2<Ent>> Database.getMinions(leader: Leader<A>): Sequence<Minion<A>> {
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
