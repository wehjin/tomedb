package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

class CardinalityMap {

    operator fun set(nameValue: Value<*>?, cardinalityValue: Value<*>?) {
        if (cardinalityValue != null && nameValue != null) {
            val cardKeyword = cardinalityValue.toTypeP<Keyword>()
            val nameKeyword = nameValue.toTypeP<Keyword>()
            nameKeyword?.let { this[it] = Cardinality.valueOf(cardKeyword) }
        }
    }

    operator fun set(keyword: Keyword, cardinality: Cardinality?) {
        if (cardinality == null) {
            map.remove(keyword.keywordHashCode())
        } else {
            map[keyword.keywordHashCode()] = cardinality
        }
    }

    operator fun get(keyword: Keyword): Cardinality {
        return map[keyword.keywordHashCode()] ?: Cardinality.ONE
    }

    override fun toString(): String = "$map"

    private val map = Scheme.cardinalities.mapKeys { it.key.keywordHashCode() }.toMutableMap()
}