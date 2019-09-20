package com.rubyhuntersky.demolib.notebook

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

object Note : AttributeGroup {

    object CREATED : Attribute {
        override val valueType: ValueType =
            ValueType.INSTANT
        override val cardinality: Cardinality =
            Cardinality.ONE
        override val description: String = "The instant a note was created."
    }

    object TEXT : Attribute {
        override val valueType: ValueType =
            ValueType.STRING
        override val cardinality: Cardinality =
            Cardinality.ONE
        override val description: String = "The text of the note."
    }
}