package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

enum class Cardinality : Keyword {
    ONE,
    MANY;

    override fun toString(): String = toKeywordString()

    companion object {
        fun valueOf(keyword: Keyword): Cardinality = if (keyword.keywordEquals(ONE)) ONE else MANY
    }
}