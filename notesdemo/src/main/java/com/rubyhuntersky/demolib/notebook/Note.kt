package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import java.util.*

object Note : AttributeGroup {

    object CREATED : Attribute<Date> {
        override val valueType = ValueType.INSTANT
        override val cardinality = Cardinality.ONE
        override val description = "The instant a note was created."
    }

    object TEXT : Attribute<String> {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "The text of the note."
    }
}