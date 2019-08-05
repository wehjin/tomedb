package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.*

interface Attribute : Keyword {
    val valueType: ValueType
    val cardinality: Cardinality
    val description: String

    fun toTagList(): TagList = tagListOf(
        Scheme.NAME..this,
        valueType at Scheme.VALUETYPE,
        cardinality at Scheme.CARDINALITY,
        description at Scheme.DESCRIPTION
    )
}