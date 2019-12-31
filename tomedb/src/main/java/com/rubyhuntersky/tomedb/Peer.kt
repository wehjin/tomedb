package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.database.Database

interface Peer<A : Attribute2<T>, T : Any> : EntDataHolder {
    val badge: Badge<A, T>
}

val <A : Attribute2<T>, T : Any> Peer<A, T>.unform: List<Form<*>>
    get() = this.reform { badge.attr set null }

data class Badge<A : Attribute2<T>, T : Any>(
    val attr: A,
    val quant: T
)

inline fun <A : Attribute2<T>, reified T : Any> Database.getPeers(badgeAttr: A): Sequence<Peer<A, T>> =
    getEntDataPairs(badgeAttr.toKeyword())
        .map { (ent, data) ->
            object : Peer<A, T> {
                override val ent: Long = ent
                override val data: Map<Keyword, Any> = data
                override val badge = Badge(badgeAttr, get(badgeAttr)!!)
            }
        }

inline fun <A : Attribute2<T>, reified T : Any> Tomic.peers(badge: A): Set<Peer<A, T>> {
    return visitPeers(badge) { peers }
}

inline fun <A : Attribute2<T>, reified T : Any, R> Tomic.visitPeers(
    badge: A,
    noinline block: PeerPack<A, T>.() -> R
): R = collectPeers(badge).visit(block)

inline fun <A : Attribute2<T>, reified T : Any> Tomic.collectPeers(badgeAttr: A): PeerPack<A, T> {
    val basis: Database = latest
    return object : PeerPack<A, T> {
        override val basis: Database = basis
        override val peers: Set<Peer<A, T>> by lazy { basis.getPeers(badgeAttr).toSet() }
        override val peersByEnt: Map<Long, Peer<A, T>> by lazy { peers.associateBy(Peer<A, T>::ent) }
        override val peersByBadge: Map<T, Peer<A, T>> by lazy { peers.associateBy { it.badge.quant } }
        override val peerOrNull: Peer<A, T>? by lazy { peers.firstOrNull() }
        override val peerList: List<Peer<A, T>> by lazy { peers.toList() }
        override fun peer(badge: T): Peer<A, T> = checkNotNull(peersByBadge[badge])
        override fun peerOrNull(badge: T): Peer<A, T>? = peersByBadge[badge]
        override fun <R> visit(block: PeerPack<A, T>.() -> R): R = run(block)
    }
}

inline fun <A : Attribute2<T>, reified T : Any, R> Tomic.reformPeers(
    badgeAttribute: A,
    noinline block: PeerPackReformScope<A, T>.() -> R
): R {
    var pack = collectPeers(badgeAttribute)
    val packReform = object : PeerPackReformScope<A, T> {
        override val basis: Database get() = pack.basis
        override val peers: Set<Peer<A, T>> get() = pack.peers
        override val peersByEnt: Map<Long, Peer<A, T>> get() = pack.peersByEnt
        override val peersByBadge: Map<T, Peer<A, T>> get() = pack.peersByBadge
        override val peerOrNull: Peer<A, T>? get() = pack.peerOrNull
        override val peerList: List<Peer<A, T>> get() = pack.peerList
        override fun peer(badge: T): Peer<A, T> = pack.peer(badge)
        override fun peerOrNull(badge: T): Peer<A, T>? = pack.peerOrNull(badge)
        override fun <R> visit(block: PeerPack<A, T>.() -> R): R = pack.visit(block)

        override var reforms: List<Form<*>> = emptyList()
            set(value) {
                check(field.isEmpty())
                field = value.also { write(value) }
                pack = collectPeers(badgeAttribute)
            }

        override fun formPeer(
            badge: T,
            ent: Long,
            init: EntReformScope.() -> Unit
        ): List<Form<*>> {
            return reformEnt(ent) {
                badgeAttribute set badge
                this.init()
            }
        }
    }
    return packReform.run(block)
}
