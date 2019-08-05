package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.*

interface Attribute : Keyword {

    val groupName: String
        get() = keywordGroup
    val itemName: String
        get() = keywordName

    val valueType: ValueType
    val cardinality: Cardinality
    val description: String

    fun toSchemeData(): TagList = tagListOf(
        Scheme.NAME..this,
        valueType at Scheme.VALUETYPE,
        cardinality at Scheme.CARDINALITY,
        description at Scheme.DESCRIPTION
    )
}
