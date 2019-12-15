package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword

sealed class Scheme<T : Any> : Attribute<T> {

    override fun toString(): String = attrName.toString()
    override val itemName: String get() = fallbackItemName
    override val groupName: String get() = fallbackGroupName

    companion object {
        private val attrs by lazy {
            listOf(NAME, VALUETYPE, CARDINALITY, DESCRIPTION)
        }

        val cardinalities: Map<Keyword, Cardinality> by lazy {
            attrs.associateBy(Attribute<*>::attrName, Attribute<*>::cardinality)
        }
    }

    object NAME : Scheme<Keyword>() {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description = "The unique name of an attribute."

    }

    object VALUETYPE : Scheme<Keyword>() {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description = "The type of the value that can be associated with a value."

    }

    object CARDINALITY : Scheme<Keyword>() {
        override val valueType = ValueType.KEYWORD
        override val cardinality = Cardinality.ONE
        override val description =
            "Specifies whether an attribute associates an entity with a single value or a set of values."

    }

    object DESCRIPTION : Scheme<String>() {
        override val valueType = ValueType.STRING
        override val cardinality = Cardinality.ONE
        override val description = "Specifies a documentation string"

    }
}


