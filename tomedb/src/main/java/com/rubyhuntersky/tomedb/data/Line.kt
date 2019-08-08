package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A line is a binding between an attribute and a value.
 * It is a component of a page and is created from
 * projections stored in the database.
 */
typealias Line<T> = Pair<Keyword, T>

val <T : Any> Line<T>.lineAttr: Keyword get() = this.first
val <T : Any> Line<T>.lineValue: T get() = this.second
fun <T : Any> Line<T>.bindEnt(ent: Ent): Projection<T> = Projection(ent.long, this.first, this.second)

fun <T : Any> lineOf(entry: Map.Entry<Keyword, T>): Line<T> = lineOf(entry.key, entry.value)
fun <T : Any> lineOf(attr: Keyword, value: T): Line<T> = attr to value
fun <T : Any> lineOf(attr: Attribute, value: T): Line<T> = attr.attrName to value
