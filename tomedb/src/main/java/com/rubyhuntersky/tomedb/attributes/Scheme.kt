package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

enum class Scheme : Attribute {

    NAME {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description = "The unique name of an attribute."
    },
    VALUETYPE {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description = "The type of the value that can be associated with a value."
    },
    CARDINALITY {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description =
            "Specifies whether an attribute associates an entity with a single value or a set of values."
    },
    DESCRIPTION {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "Specifies a documentation string"
    };

    override fun toString(): String = attrName.toString()

    companion object {
        val cardinalities: Map<Keyword, Cardinality> =
            values().associateBy(Scheme::attrName, Scheme::cardinality)
    }
}


