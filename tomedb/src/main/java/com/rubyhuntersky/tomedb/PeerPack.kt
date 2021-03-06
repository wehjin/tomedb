package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.database.Database
import kotlin.math.absoluteValue
import kotlin.random.Random

interface PeerPack<A : Attribute2<T>, T : Any> {
    val basis: Database
    val peers: Set<Peer<A, T>>
    val peersByEnt: Map<Long, Peer<A, T>>
    val peersByBadge: Map<T, Peer<A, T>>
    val peerOrNull: Peer<A, T>?
    val peerList: List<Peer<A, T>>
    fun peer(badge: T): Peer<A, T>
    fun peerOrNull(badge: T): Peer<A, T>?
    fun <R> visit(block: PeerPack<A, T>.() -> R): R
}


interface PeerPackReformScope<A : Attribute2<T>, T : Any> : PeerPack<A, T> {

    var reforms: List<Form<*>>

    fun formPeer(
        badge: T,
        ent: Long = Random.nextLong().absoluteValue,
        init: EntReformScope.() -> Unit = {}
    ): List<Form<*>>
}
