package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

sealed class Scheme : Attribute {

    override fun toString(): String = attrName.toString()

    companion object {
        private val attrs by lazy {
            listOf(NAME, VALUETYPE, CARDINALITY, DESCRIPTION)
        }

        val cardinalities: Map<Keyword, Cardinality> by lazy {
            attrs.associateBy(Scheme::attrName, Scheme::cardinality)
        }
    }

    object NAME : Scheme() {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description = "The unique name of an attribute."

    }

    object VALUETYPE : Scheme() {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description = "The type of the value that can be associated with a value."

    }

    object CARDINALITY : Scheme() {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description =
            "Specifies whether an attribute associates an entity with a single value or a set of values."

    }

    object DESCRIPTION : Scheme() {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "Specifies a documentation string"

    }
}


