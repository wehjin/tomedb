package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizzerTest {

    enum class Learner(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Name(ValueType.STRING, Cardinality.ONE, "The name of the learner"),
        Selected(ValueType.BOOLEAN, Cardinality.ONE, "The selected learner"),
        Quiz(ValueType.REF, Cardinality.MANY, "A quiz held by the learner");
    }

    enum class Quiz(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Name(ValueType.STRING, Cardinality.ONE, "The name of the quiz"),
        Publisher(ValueType.STRING, Cardinality.ONE, "The name of the publisher who created the quiz"),
        Lesson(ValueType.REF, Cardinality.MANY, "A lesson in the quiz"),
        CompletedOn(ValueType.DATE, Cardinality.ONE, "Time the quiz was completed");
    }

    enum class Lesson(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Question(ValueType.STRING, Cardinality.ONE, "The question of the lesson"),
        Answer(ValueType.STRING, Cardinality.ONE, "The answer of the lesson");
    }

    private val findSelectedLearners = Query.Find(
        listOf("e"),
        listOf(Rule.EinAV("e", Learner.Selected, Value.BOOLEAN(true)))
    )

    private val findQuizzes = Query.Find(
        listOf("quiz", "name"),
        listOf(
            Rule.EinA("quiz", Quiz.Name),
            Rule.EinAV("selectedLearner", Learner.Selected, Value.BOOLEAN(true)),
            Rule.EEinA("selectedLearner", "quiz", Learner.Quiz),
            Rule.EVinA("quiz", "name", Quiz.Name)
        )
    )

    private val learnerData = listOf(
        Pair(Learner.Name, Value.STRING("Default")),
        Pair(Learner.Selected, Value.BOOLEAN(true)),
        Pair(
            Learner.Quiz, Value.DATA(
                listOf(
                    Pair(Quiz.Name, Value.STRING("Basics")),
                    Pair(Quiz.Publisher, Value.STRING("Life")),
                    Pair(
                        Quiz.Lesson, Value.DATA(
                            listOf(
                                Pair(Lesson.Question, Value.STRING("Hello?")),
                                Pair(Lesson.Answer, Value.STRING("World"))
                            )
                        )
                    ),
                    Pair(
                        Quiz.Lesson, Value.DATA(
                            listOf(
                                Pair(Lesson.Question, Value.STRING("Moshi moshi ka")),
                                Pair(Lesson.Answer, Value.STRING("Sekkai"))
                            )
                        )
                    )
                )
            )
        ),
        Pair(
            Learner.Quiz, Value.DATA(
                listOf(
                    Pair(Quiz.Name, Value.STRING("Advanced")),
                    Pair(Quiz.Publisher, Value.STRING("Life")),
                    Pair(
                        Quiz.Lesson, Value.DATA(
                            listOf(
                                Pair(Lesson.Question, Value.STRING("World?")),
                                Pair(Lesson.Answer, Value.STRING("Hello"))
                            )
                        )
                    ),
                    Pair(
                        Quiz.Lesson, Value.DATA(
                            listOf(
                                Pair(Lesson.Question, Value.STRING("Sekkai ka")),
                                Pair(Lesson.Answer, Value.STRING("Moshi moshi"))
                            )
                        )
                    )
                )
            )
        )
    )

    @Test
    fun happy() {
        val conn = Client().connect("quizzer")
        conn.transactAttributes(*Lesson.values(), *Quiz.values(), *Learner.values())
        assertEquals(0, conn.database.query(findSelectedLearners).size)

        conn.transactData(listOf(learnerData))
        assertEquals(1, conn.database.query(findSelectedLearners).size)

        val quizzes = conn.database.query(findQuizzes)
        println("QUIZZES: $quizzes")
        assertEquals(2, quizzes.size)
        assertEquals(setOf("Basics", "Advanced"), quizzes.map { (it["name"] as Value.STRING).v }.toSet())
    }
}