package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.database.Database

interface Peer<A : Attribute2<T>, T : Any> : EntHolder {
    override val ent: Long
    val badge: Badge<A, T>
    operator fun <U : Any> get(attribute: Attribute2<U>): U?
}

data class Badge<A : Attribute2<T>, T : Any>(
    val attr: A,
    val quant: T
)

inline fun <A : Attribute2<T>, reified T : Any> Database.getPeers(badgeAttr: A): Sequence<Peer<A, T>> =
    getEntDataPairs(badgeAttr.toKeyword())
        .map { (ent, data) ->
            object : Peer<A, T> {
                override val ent: Long = ent
                override val badge = Badge(badgeAttr, get(badgeAttr)!!)
                override fun <U : Any> get(attribute: Attribute2<U>): U? {
                    return data[attribute.toKeyword()]?.let {
                        attribute.scriber.unscribe(it as String)
                    }
                }
            }
        }

inline fun <A : Attribute2<T>, reified T : Any> Tomic.peers(badge: A): List<Peer<A, T>> =
    visitPeers(badge) { peersByEnt.values.toList() }

inline fun <A : Attribute2<T>, reified T : Any, R> Tomic.visitPeers(
    badge: A,
    noinline block: PeerPack<A, T>.() -> R
): R = collectPeers(badge).visit(block)

inline fun <A : Attribute2<T>, reified T : Any> Tomic.collectPeers(badgeAttr: A): PeerPack<A, T> {
    val basis: Database = getDb()
    return object : PeerPack<A, T> {
        override val basis: Database = basis
        override val peers: Set<Peer<A, T>> by lazy { basis.getPeers(badgeAttr).toSet() }
        override val peersByEnt: Map<Long, Peer<A, T>> by lazy { peers.associateBy(Peer<A, T>::ent) }
        override val peersByBadge: Map<T, Peer<A, T>> by lazy { peers.associateBy { it.badge.quant } }
        override val peerOrNull: Peer<A, T>? by lazy { peers.firstOrNull() }
        override val peerList: List<Peer<A, T>> by lazy { peersByEnt.values.toList() }
    }
}

inline fun <A : Attribute2<T>, reified T : Any, R> Tomic.reformPeers(
    badgeAttribute: A,
    noinline block: PeerPackReformScope<A, T>.() -> R
): R {
    var hive = collectPeers(badgeAttribute)
    val mutableHive = object : PeerPackReformScope<A, T> {
        override val basis: Database get() = hive.basis
        override val peers: Set<Peer<A, T>> get() = hive.peers
        override val peersByEnt: Map<Long, Peer<A, T>> get() = hive.peersByEnt
        override val peersByBadge: Map<T, Peer<A, T>> get() = hive.peersByBadge
        override val peerOrNull: Peer<A, T>? get() = hive.peerOrNull
        override val peerList: List<Peer<A, T>> get() = hive.peerList

        override var reforms: List<Form<*>> = emptyList()
            set(value) {
                check(field.isEmpty())
                field = value.also { write(value) }
                hive = collectPeers(badgeAttribute)
            }

        override fun formPeer(
            quant: T,
            ent: Long,
            init: EntReformScope.() -> Unit
        ): List<Form<*>> {
            return reformEnt(ent) {
                badgeAttribute set quant
                this.init()
            }
        }
    }
    return mutableHive.run(block)
}
