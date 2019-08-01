package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.*
import com.rubyhuntersky.tomedb.connection.ConnectionStarter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.file.Path

class QuizzerTest {

    private lateinit var dataDir: Path

    @Before
    fun setUp() {
        dataDir = TempDirFixture.initDir("quizzerTest")
    }

    @Test
    fun happy() {
        val specs = ConnectionStarter.Attributes(listOf(*Lesson.values(), *Quiz.values(), *Learner.values()))

        val conn = Client().connect(dataDir, specs)
        val findSelectedLearners = Query.Find(
            rules = listOf(Rule.EExactVM("e", Value.BOOLEAN(true), Learner.Selected)),
            outputs = listOf("e")
        )
        assertEquals(0, conn.database(findSelectedLearners).size)

        conn.transactData(listOf(learnerData))
        assertEquals(1, conn.database(findSelectedLearners).size)

        val quizResults = conn.database(
            Query.Find(
                rules = listOf(
                    Rule.EExactVM("selectedLearner", Value.BOOLEAN(true), Learner.Selected),
                    Rule.EEExactM("selectedLearner", "quiz", Learner.Quiz),
                    Rule.EVExactM("quiz", "name", Quiz.Name)
                ),
                outputs = listOf("quiz", "name")
            )
        )
        println("QUIZZES: $quizResults")
        assertEquals(2, quizResults.size)

        assertEquals(
            setOf("Basics", "Advanced"),
            quizResults.map { it["name"].asString() }.toSet()
        )

        val query = Query.Find(
            rules = listOf(
                Rule.EEExactM("selectedQuiz", "lesson", Quiz.Lesson),
                Rule.EVExactM("lesson", "question", Lesson.Question),
                Rule.EVExactM("lesson", "answer", Lesson.Answer)
            ),
            inputs = listOf(
                Input(
                    "selectedQuiz",
                    quizResults.first { it["name"].asString() == "Basics" }["quiz"].asLong()
                )
            ),
            outputs = listOf("lesson", "question", "answer")
        )
        val lessons = conn.database(query)
        println("LESSONS: $lessons")
        assertEquals(2, lessons.size)
        assertEquals(
            setOf("World", "Sekkai"),
            lessons.map { it["answer"].asString() }.toSet()
        )
    }

    private val learnerData = tagListOf(
        "Default" at Learner.Name,
        true at Learner.Selected,
        tagListOf(
            "Basics" at Quiz.Name,
            "Life" at Quiz.Publisher,
            tagListOf(
                "Hello?" at Lesson.Question,
                "World" at Lesson.Answer
            ) at Quiz.Lesson,
            tagListOf(
                "Moshi moshi ka" at Lesson.Question,
                "Sekkai" at Lesson.Answer
            ) at Quiz.Lesson
        ) at Learner.Quiz,
        tagListOf(
            "Advanced" at Quiz.Name,
            "Life" at Quiz.Publisher,
            tagListOf(
                "World?" at Lesson.Question,
                "Hello" at Lesson.Answer
            ) at Quiz.Lesson,
            tagListOf(
                "Sekkai ka" at Lesson.Question,
                "Moshi moshi" at Lesson.Answer
            ) at Quiz.Lesson
        ) at Learner.Quiz
    )
}