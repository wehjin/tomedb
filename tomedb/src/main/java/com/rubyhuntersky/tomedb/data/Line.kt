package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A line is a binding between an attribute and a value.
 * It is a component of a page and is created from
 * projections stored in the database.
 */
typealias Line<T> = Pair<Keyword, T>

val <T : Any> Line<T>.lineAttr: Keyword get() = this.first
val <T : Any> Line<T>.lineValue: T get() = this.second

fun <T : Any> lineOf(attr: Keyword, value: T): Line<T> {
    return attr to value
}
