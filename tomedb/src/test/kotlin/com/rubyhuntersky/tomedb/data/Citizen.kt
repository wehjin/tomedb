package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.*

object Citizen : AttributeGroup {
    object FullName : Attribute<String> {
        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "The name of a citizen."
    }

    object Country : Attribute<Long> {
        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
        override val valueType = ValueType.LONG
        override val cardinality = Cardinality.ONE
        override val description = "The country of residence for a citizen."
    }
}