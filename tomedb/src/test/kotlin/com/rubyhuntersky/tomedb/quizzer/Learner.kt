package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

enum class Learner(
    override val valueType: ValueType<*>,
    override val cardinality: Cardinality,
    override val description: String
) : Attribute {
    Name(ValueType.STRING, Cardinality.ONE, "The name of the learner"),
    Selected(ValueType.BOOLEAN, Cardinality.ONE, "The selected learner"),
    Quiz(ValueType.LONG, Cardinality.MANY, "A quiz held by the learner");

    override fun toString(): String = attrName.toString()
}