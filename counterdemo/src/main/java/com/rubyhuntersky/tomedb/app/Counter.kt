package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

sealed class Counter<T : Any> : Attribute<T> {

    companion object : AttributeGroup {
        fun attrs() = arrayOf(Count)
    }

    object Count : Counter<Long>() {
        override val valueType = ValueType.LONG
        override val cardinality = Cardinality.ONE
        override val description = "The current count of a counter"
    }
}