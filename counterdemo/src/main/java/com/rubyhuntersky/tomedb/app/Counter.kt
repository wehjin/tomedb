package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.attributes.*

sealed class Counter<T : Any> : Attribute<T> {
    override val itemName: String get() = fallbackItemName
    override val groupName: String get() = fallbackGroupName

    companion object : AttributeGroup {
        fun attrs() = arrayOf(Count)
    }

    object Count : Counter<Long>() {
        override val valueType = ValueType.LONG
        override val cardinality = Cardinality.ONE
        override val description = "The current count of a counter"
    }
}