package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.findQuantInData
import com.rubyhuntersky.tomedb.basics.Keyword

interface EntHolder {
    val ent: Long
}

fun EntHolder.reform(
    init: EntReformScope.() -> Unit
): List<Form<*>> = reformEnt(ent, init)

interface EntDataHolder : EntHolder {
    val data: Map<Keyword, Any>
}

inline operator fun <reified U : Any> EntDataHolder.get(attribute: Attribute2<U>): U? {
    return findQuantInData(data, attribute)
}
