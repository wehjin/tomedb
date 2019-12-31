package com.rubyhuntersky.tomedb.minion

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.database.Database

fun <A : Attribute2<Ent>> Tomic.formMinion(
    leader: Leader<A>,
    ent: Long = randomEnt(),
    init: EntReformScope.() -> Unit
): Minion<A> = reformMinions(leader) {
    reforms = formMinion(ent, init)
    minion(ent)
}

fun <A : Attribute2<Ent>> Tomic.reformMinion(
    leader: Leader<A>,
    ent: Long,
    reform: MinionReformScope<A>.() -> Unit
): Minion<A> = reformMinions(leader) {
    reforms = reformMinion(ent, reform)
    minion(ent)
}

fun <A : Attribute2<Ent>> Tomic.unformMinion(
    leader: Leader<A>,
    ent: Long
): Minion<A>? = reformMinions(leader) {
    val minion = minionOrNull(ent)
    if (minion != null) {
        reforms = unform(minion)
    }
    minion
}

fun <A : Attribute2<Ent>, R> Tomic.reformMinions(
    leader: Leader<A>,
    block: MinionMobReformScope<A>.() -> R
): R {
    var mob = latest.collectMinions(leader)
    val mobReform = object :
        MinionMobReformScope<A> {
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
                mob = latest.collectMinions(leader)
            }

        override fun formMinion(ent: Long, init: EntReformScope.() -> Unit): List<Form<*>> {
            return reformEnt(ent) {
                leader.attr set Ent(leader.ent)
                init()
            }
        }

        override fun reformMinion(
            ent: Long,
            reform: MinionReformScope<A>.() -> Unit
        ): List<Form<*>> {
            val reforms = minionOrNull(ent)?.let { minion ->
                if (minion.hasLeader(leader)) {
                    reformEnt(ent) {
                        object : MinionReformScope<A> {
                            override val minion: Minion<A> = minion
                            override fun <T : Any> bind(attribute: Attribute2<T>, quant: T?) {
                                this@reformEnt.bind(attribute, quant)
                            }

                            override fun <T : Any> Attribute2<T>.set(quant: T?) {
                                this@reformEnt.run { set(quant) }
                            }
                        }.reform()
                    }
                } else null
            }
            return reforms ?: emptyList()
        }
    }
    return mobReform.run(block)
}
