package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

interface GroupedItem {

    val itemName: String
        get() = (this as? Enum<*>)?.let { this.name } ?: this::class.java.simpleName

    val groupName: String
        get() = (this as? Enum<*>)?.let {
            this::class.java.simpleName.let {
                if (it != name) it else this::class.java.enclosingClass?.simpleName ?: ""
            }
        } ?: this::class.java.declaringClass?.simpleName ?: ""

    fun toGroupedItemString(): String = "$groupName/$itemName"

    fun groupedItemEquals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Keyword) return false
        if (itemName != other.keywordName) return false
        if (groupName != other.keywordGroup) return false
        return true
    }

    fun groupedItemHashCode(): Int {
        var result = this.itemName.hashCode()
        result = 31 * result + this.groupName.hashCode()
        return result
    }

    fun toKeyword(): Keyword = Keyword(itemName, groupName)
}