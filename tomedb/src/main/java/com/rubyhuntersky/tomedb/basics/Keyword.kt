package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.GroupedItem
import com.rubyhuntersky.tomedb.attributes.groupedItemEquals
import com.rubyhuntersky.tomedb.attributes.groupedItemHashCode
import com.rubyhuntersky.tomedb.attributes.toGroupedItemString

data class Keyword(
    val keywordName: String,
    val keywordGroup: String
) : GroupedItem {
    constructor(keyword: Keyword) : this(keyword.keywordName, keyword.keywordGroup)

    override val itemName: String = keywordName
    override val groupName: String = keywordGroup
    override fun toString(): String = toGroupedItemString()
    override fun equals(other: Any?): Boolean = groupedItemEquals(other)
    override fun hashCode(): Int = groupedItemHashCode()
}