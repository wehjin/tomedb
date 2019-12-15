package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.*
import java.util.*

object Note : AttributeGroup {

    object CREATED : Attribute<Date> {
        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
        override val valueType = ValueType.INSTANT
        override val cardinality = Cardinality.ONE
        override val description = "The instant a note was created."
    }

    object TEXT : Attribute<String> {
        override val itemName: String get() = fallbackItemName
        override val groupName: String get() = fallbackGroupName
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "The text of the note."
    }
}