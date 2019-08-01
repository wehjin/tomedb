package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.MeterSpec
import com.rubyhuntersky.tomedb.Cardinality
import com.rubyhuntersky.tomedb.basics.ValueType

enum class Lesson(
    override val valueType: ValueType,
    override val cardinality: Cardinality,
    override val description: String
) : MeterSpec {
    Question(ValueType.STRING, Cardinality.ONE, "The question of the lesson"),
    Answer(ValueType.STRING, Cardinality.ONE, "The answer of the lesson");
}