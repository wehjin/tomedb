package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.tagListOf
import com.rubyhuntersky.tomedb.basics.tagOf

interface Attribute<ValueT : Any> : GroupedItem {

    val attrName: Keyword
        get() = toKeyword()

    val valueType: ValueType<*>
    val cardinality: Cardinality
    val description: String

    infix fun to(value: ValueT) = Pair(attrName, value)

    fun toSchemeData(): TagList = tagListOf(
        tagOf(attrName, Scheme.NAME.attrName),
        tagOf(valueType.keyword, Scheme.VALUETYPE.attrName),
        tagOf(cardinality.keyword, Scheme.CARDINALITY.attrName),
        tagOf(description, Scheme.DESCRIPTION.attrName)
    )
}

