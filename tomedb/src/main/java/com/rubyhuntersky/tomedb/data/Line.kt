package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A line is a binding between an attribute and a value.
 * It is a component of a page and is created from
 * projections stored in the database.
 */
data class Line<T : Any>(val attr: Keyword, val value: T)