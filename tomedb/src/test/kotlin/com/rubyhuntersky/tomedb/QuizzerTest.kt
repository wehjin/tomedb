package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.ValueType
import com.rubyhuntersky.tomedb.basics.asLong
import com.rubyhuntersky.tomedb.basics.asString
import com.rubyhuntersky.tomedb.connection.ConnectionStarter
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
        val starterAttributes =
            ConnectionStarter.Attributes(listOf(*Lesson.values(), *Quiz.values(), *Learner.values()))

        val conn = Client().connect(starterAttributes, TransientLedgerWriter())
        val findSelectedLearners = Query.Find(
            rules = listOf(Rule.EExactVA("e", Value.BOOLEAN(true), Learner.Selected)),
            outputs = listOf("e")
        )
        assertEquals(0, conn.database[findSelectedLearners].size)

        conn.transactData(listOf(learnerData))
        assertEquals(1, conn.database[findSelectedLearners].size)

        val quizResults = conn.database[Query.Find(
            rules = listOf(
                Rule.EExactVA("selectedLearner", Value.BOOLEAN(true), Learner.Selected),
                Rule.EEExactA("selectedLearner", "quiz", Learner.Quiz),
                Rule.EVExactA("quiz", "name", Quiz.Name)
            ),
            outputs = listOf("quiz", "name")
        )]
        println("QUIZZES: $quizResults")
        assertEquals(2, quizResults.size)

        assertEquals(
            setOf("Basics", "Advanced"),
            quizResults.map { it["name"].asString() }.toSet()
        )

        val lessons = conn.database[Query.Find(
            rules = listOf(
                Rule.EEExactA("selectedQuiz", "lesson", Quiz.Lesson),
                Rule.EVExactA("lesson", "question", Lesson.Question),
                Rule.EVExactA("lesson", "answer", Lesson.Answer)
            ),
            inputs = listOf(
                Input(
                    "selectedQuiz",
                    quizResults.first { it["name"].asString() == "Basics" }["quiz"].asLong()
                )
            ),
            outputs = listOf("lesson", "question", "answer")
        )]
        println("LESSONS: $lessons")
        assertEquals(2, lessons.size)
        assertEquals(
            setOf("World", "Sekkai"),
            lessons.map { it["answer"].asString() }.toSet()
        )
    }
}