package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

object Citizen : AttributeGroup {
    object FullName : Attribute {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "The name of a citizen."
    }

    object Country : Attribute {
        override val valueType = ValueType.LONG
        override val cardinality = Cardinality.ONE
        override val description = "The country of residence for a citizen."
    }
}