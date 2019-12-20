package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.database.Database

data class Badge<A : Attribute2<T>, T : Any>(
    val attr: A,
    val quant: T
)

interface Peer<A : Attribute2<T>, T : Any> : EntHolder {
    override val ent: Long
    val badge: Badge<A, T>
    operator fun <U : Any> get(attribute: Attribute2<U>): U?
}

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
