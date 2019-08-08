package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

enum class Counter : Attribute {

    Count {
        override val valueType: ValueType = ValueType.LONG
        override val cardinality: Cardinality = Cardinality.ONE
        override val description: String = "The current count of a counter"
    };

    companion object : AttributeGroup
}