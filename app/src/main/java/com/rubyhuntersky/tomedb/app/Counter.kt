package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Cardinality
import com.rubyhuntersky.tomedb.basics.ValueType

enum class Counter : Attribute {
    Count {
        override val valueType: ValueType =
            ValueType.LONG
        override val cardinality: Cardinality = Cardinality.ONE
        override val description: String = "The current count of a counter"
    }
}