package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

enum class Cardinality {
    ONE,
    MANY;

    val keyword: Keyword
            by lazy { Keyword(this.name, Cardinality::class.java.simpleName) }

    override fun toString(): String = keyword.toString()

    companion object {
        fun valueOf(keyword: Keyword?): Cardinality = if (MANY.keyword == keyword) MANY else ONE
    }
}