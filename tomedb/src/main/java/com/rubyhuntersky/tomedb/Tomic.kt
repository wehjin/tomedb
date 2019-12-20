package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import java.io.File

interface Tomic {
    fun getDb(): Database
    fun write(mods: List<Mod<*>>)
    fun close()
}

inline fun <A : Attribute2<T>, reified T : Any> Tomic.peers(badge: A): List<Peer<A, T>> =
    visitPeers(badge) { peersByEnt.values.toList() }

inline fun <A : Attribute2<T>, reified T : Any, R> Tomic.visitPeers(
    badge: A,
    noinline block: PeerHive<A, T>.() -> R
): R = collectPeers(badge).visit(block)

inline fun <A : Attribute2<T>, reified T : Any> Tomic.collectPeers(badgeAttr: A): PeerHive<A, T> {
    val basis: Database = getDb()
    return object : PeerHive<A, T> {
        override val basis: Database = basis
        override val peers: Set<Peer<A, T>> by lazy { basis.getPeers(badgeAttr).toSet() }
        override val peersByEnt: Map<Long, Peer<A, T>> by lazy { peers.associateBy(Peer<A, T>::ent) }
        override val peersByBadge: Map<T, Peer<A, T>> by lazy { peers.associateBy { it.badge.quant } }
        override val peerOrNull: Peer<A, T>? by lazy { peers.firstOrNull() }
        override val peerList: List<Peer<A, T>> by lazy { peersByEnt.values.toList() }
    }
}

inline fun <A : Attribute2<T>, reified T : Any, R> Tomic.modPeers(
    property: A,
    noinline block: MutablePeerHive<A, T>.() -> R
): R {
    var hive = collectPeers(property)
    val mutableHive = object : MutablePeerHive<A, T> {
        override val basis: Database get() = hive.basis
        override val peers: Set<Peer<A, T>> get() = hive.peers
        override val peersByEnt: Map<Long, Peer<A, T>> get() = hive.peersByEnt
        override val peersByBadge: Map<T, Peer<A, T>> get() = hive.peersByBadge
        override val peerOrNull: Peer<A, T>? get() = hive.peerOrNull
        override val peerList: List<Peer<A, T>> get() = hive.peerList

        override var mods: List<Mod<*>> = emptyList()
            set(value) {
                check(field.isEmpty())
                field = value.also { write(value) }
                hive = collectPeers(property)
            }
    }
    return mutableHive.run(block)
}

fun tomicOf(dir: File, init: TomicScope.() -> List<Attribute<*>>): Tomic {
    val spec = object : TomicScope {}.init()
    val session = startSession(dir, spec)
    return object : Tomic {
        override fun close() = session.close()
        override fun getDb(): Database = session.getDb()

        override fun write(mods: List<Mod<*>>) {
            val updates = mods.map {
                val updateType = when (it) {
                    is Mod.Set -> UpdateType.Declare
                    is Mod.Clear -> UpdateType.Retract
                }
                Update(it.ent, it.attribute.toKeyword(), it.quantAsScript(), updateType)
            }
            session.transactDb(updates.toSet())
        }
    }
}

interface TomicScope
