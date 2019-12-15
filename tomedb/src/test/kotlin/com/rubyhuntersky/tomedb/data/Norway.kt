package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.*

@Suppress("unused")
object Norway : AttributeGroup {
    object Fjords : Attribute<Long> {
        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
        override val valueType = ValueType.LONG
        override val cardinality = Cardinality.ONE
        override val description = "The number of fjords in a Norway."
    }
}