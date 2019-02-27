package com.rubyhuntersky.tomedb

import org.junit.Test

class QuizzerTest {

    enum class Learner(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Name(ValueType.STRING, Cardinality.ONE, "The name of the learner"),
        Quiz(ValueType.REF, Cardinality.MANY, "A quiz held by the learner")
    }

    enum class Quiz(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Name(ValueType.STRING, Cardinality.ONE, "The name of the quiz"),
        Publisher(ValueType.STRING, Cardinality.ONE, "The name of the publisher who created the quiz"),
        Lesson(ValueType.REF, Cardinality.MANY, "A lesson in the quiz"),
        CompletedOn(ValueType.DATE, Cardinality.ONE, "Time the quiz was completed")
    }

    enum class Lesson(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Question(ValueType.STRING, Cardinality.ONE, "The question of the lesson"),
        Answer(ValueType.STRING, Cardinality.ONE, "The answer of the lesson")
    }

    @Test
    fun happy() {
        val conn = Client().connect("chichi")
        conn.transactAttributes(*Lesson.values(), *Quiz.values(), *Learner.values())
    }
}