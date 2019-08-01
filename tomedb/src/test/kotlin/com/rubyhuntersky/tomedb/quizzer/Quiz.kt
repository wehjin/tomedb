package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.AttrSpec
import com.rubyhuntersky.tomedb.Cardinality
import com.rubyhuntersky.tomedb.basics.ValueType

enum class Quiz(
    override val valueType: ValueType,
    override val cardinality: Cardinality,
    override val description: String
) : AttrSpec {
    Name(ValueType.STRING, Cardinality.ONE, "The name of the quiz"),
    Publisher(ValueType.STRING, Cardinality.ONE, "The name of the publisher who created the quiz"),
    Lesson(ValueType.LONG, Cardinality.MANY, "A lesson in the quiz"),
    CompletedOn(ValueType.INSTANT, Cardinality.ONE, "Time the quiz was completed");
}