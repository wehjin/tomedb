package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.basics.Keyword

sealed class Filter {
    abstract fun passesData(data: Map<Keyword, Any>): Boolean
}

data class BadgeFilter<A : Attribute2<T>, T : Any>(val attr: A) : Filter() {

    override fun passesData(data: Map<Keyword, Any>): Boolean {
        return data[attr.toKeyword()] != null
    }
}

data class SumFilter(val filters: List<Filter>) : Filter() {
    override fun passesData(data: Map<Keyword, Any>): Boolean {
        return filters.fold(
            initial = false,
            operation = { passed, next ->
                if (passed) true
                else next.passesData(data)
            }
        )
    }
}

fun anyOf(vararg filters: Filter): Filter = SumFilter(filters.toList())
