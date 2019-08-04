package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

enum class Cardinality : Keyword {
    ONE,
    MANY;

    override fun toString(): String = toKeywordString()
}