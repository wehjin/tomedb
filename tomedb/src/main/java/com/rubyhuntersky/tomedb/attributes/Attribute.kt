package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.at
import com.rubyhuntersky.tomedb.basics.tagListOf

interface Attribute : GroupedItem {

    val attrName: Keyword
        get() = Keyword(itemName, groupName)

    val valueType: ValueType
    val cardinality: Cardinality
    val description: String

    infix fun <T : Any> to(other: T) = Pair(attrName, other)

    fun toSchemeData(): TagList = tagListOf(
        attrName at Scheme.NAME,
        valueType.keyword at Scheme.VALUETYPE,
        cardinality.keyword at Scheme.CARDINALITY,
        description at Scheme.DESCRIPTION
    )
}

