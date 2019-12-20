package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.database.Database

interface PeerHive<A : Attribute2<T>, T : Any> {
    val basis: Database
    val peers: Set<Peer<A, T>>
    val peersByEnt: Map<Long, Peer<A, T>>
    val peersByBadge: Map<T, Peer<A, T>>
    val peerOrNull: Peer<A, T>?
    val peerList: List<Peer<A, T>>
}

fun <A : Attribute2<T>, T : Any, R> PeerHive<A, T>.visit(
    block: PeerHive<A, T>.() -> R
): R = run(block)

interface MutablePeerHive<A : Attribute2<T>, T : Any> : PeerHive<A, T> {
    var forms: List<Form<*>>
}
