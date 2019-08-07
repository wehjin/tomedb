package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

@Suppress("unused")
object Norway : AttributeGroup {
    object Fjords : Attribute {
        override val valueType: ValueType =
            ValueType.LONG
        override val cardinality: Cardinality =
            Cardinality.ONE
        override val description: String = "The number of fjords in a Norway."
    }
}