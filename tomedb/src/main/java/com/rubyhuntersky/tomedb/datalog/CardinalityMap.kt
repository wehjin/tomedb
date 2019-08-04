package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Keyword

class CardinalityMap {

    operator fun get(keyword: Keyword): Cardinality = map[keyword] ?: Cardinality.ONE

    operator fun set(keyword: Keyword, cardinality: Cardinality?) {
        map[keyword] = cardinality ?: Cardinality.ONE
    }

    private val map = Scheme.cardinalities.toMutableMap()
}