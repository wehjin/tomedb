package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

@Suppress("unused")
object Norway : AttributeGroup {
    object Fjords : Attribute<Long> {
        override val valueType = ValueType.LONG
        override val cardinality = Cardinality.ONE
        override val description = "The number of fjords in a Norway."
    }
}