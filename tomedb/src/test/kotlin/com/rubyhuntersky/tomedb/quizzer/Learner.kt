package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Cardinality
import com.rubyhuntersky.tomedb.basics.ValueType

enum class Learner(
    override val valueType: ValueType,
    override val cardinality: Cardinality,
    override val description: String
) : Attribute {
    Name(ValueType.STRING, Cardinality.ONE, "The name of the learner"),
    Selected(ValueType.BOOLEAN, Cardinality.ONE, "The selected learner"),
    Quiz(ValueType.LONG, Cardinality.MANY, "A quiz held by the learner");
}