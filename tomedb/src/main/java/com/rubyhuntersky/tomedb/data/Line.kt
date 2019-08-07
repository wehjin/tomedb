package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A line is a binding between an attribute and a value.
 * It is a component of a page and is created from
 * projections stored in the database.
 */
typealias Line<T> = Map.Entry<Keyword, T>

fun <T : Any> lineOf(attr: Keyword, value: T): Line<T> = mapOf(attr to value).entries.first()
val <T : Any> Line<T>.attr: Keyword get() = this.key
