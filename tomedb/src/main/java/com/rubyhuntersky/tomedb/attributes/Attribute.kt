package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.tagListOf
import com.rubyhuntersky.tomedb.basics.tagOf

interface Attribute : GroupedItem {

    val attrName: Keyword
        get() = toKeyword()

    val valueType: ValueType
    val cardinality: Cardinality
    val description: String

    infix fun <T : Any> to(other: T) = Pair(attrName, other)

    fun toSchemeData(): TagList = tagListOf(
        tagOf(attrName, Scheme.NAME.attrName),
        tagOf(valueType.keyword, Scheme.VALUETYPE.attrName),
        tagOf(cardinality.keyword, Scheme.CARDINALITY.attrName),
        tagOf(description, Scheme.DESCRIPTION.attrName)
    )
}

