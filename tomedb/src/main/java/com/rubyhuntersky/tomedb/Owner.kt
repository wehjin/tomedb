package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.database.Database

interface Owner<T : Any> {
    val ent: Long
    operator fun <U : Any> get(attribute: Attribute2<U>): U?
}

inline fun <reified T : Any> getOwners(
    database: Database,
    property: Attribute2<T>
): List<Owner<T>> {
    val entDataPairs = database.getOwners(property.toKeyword()).toList()
    return entDataPairs.map { (ent, data) ->
        object : Owner<T> {
            override val ent: Long = ent
            override fun <U : Any> get(attribute: Attribute2<U>): U? {
                return data[attribute.toKeyword()]?.let {
                    attribute.scriber.unscribe(it as String)
                }
            }
        }
    }
}

fun <T : Any> Owner<T>.mod(init: EntModScope.() -> Unit): List<Mod<*>> = modEnt(ent, init)
