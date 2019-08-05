package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Keyword

class CardinalityMap {

    operator fun get(keyword: Keyword): Cardinality {
        return map[keyword.keywordHashCode()] ?: Cardinality.ONE
    }

    operator fun set(keyword: Keyword, cardinality: Cardinality?) {
        if (cardinality == null) {
            map.remove(keyword.keywordHashCode())
        } else {
            map[keyword.keywordHashCode()] = cardinality
        }
    }

    override fun toString(): String = "$map"

    private val map = Scheme.cardinalities.mapKeys { it.key.keywordHashCode() }.toMutableMap()
}