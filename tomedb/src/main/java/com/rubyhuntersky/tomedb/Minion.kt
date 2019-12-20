package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.basics.Ent

interface Minion<A : Attribute2<Ent>> : EntHolder {
    override val ent: Long
    val leader: Leader<A>
    operator fun <U : Any> get(attribute: Attribute2<U>): U?
}

data class Leader<A : Attribute2<Ent>>(
    val quant: Ent,
    val attr: A
)
