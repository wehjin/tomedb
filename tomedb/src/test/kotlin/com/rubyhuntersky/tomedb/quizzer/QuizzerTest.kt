package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Client
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.basics.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File


class QuizzerTest {

    private lateinit var dataDir: File

    @Before
    fun setUp() {
        dataDir = TempDirFixture.initDir("quizzerTest").toFile()
    }

    object SelectedLearnerSlot : Query.Find2.Slot

    @Test
    fun happy() {
        val spec: List<Attribute> = listOf(*Lesson.values(), *Quiz.values(), *Learner.values())
        val conn = Client().connect(dataDir, spec)

        val findSelectedLearners = queryOf {
            rules = listOf(
                SelectedLearnerSlot capture Learner.Selected eq true(),
                -SelectedLearnerSlot
            )
        }
        val selectedLearnersResult1 = conn.mutDb(findSelectedLearners)
        println("SELECTED LEARNERS 1: ${selectedLearnersResult1(SelectedLearnerSlot)}")
        assertEquals(0, selectedLearnersResult1.size)

        conn.transactData(listOf(learnerData))
        val selectedLearnersResult2 = conn.mutDb(findSelectedLearners)
        println("SELECTED LEARNERS 2: ${SelectedLearnerSlot(selectedLearnersResult2)}")
        assertEquals(1, selectedLearnersResult2.size)

        val quizResults = conn.mutDb(queryOf {
            rules = listOf(
                SelectedLearnerSlot capture Learner.Selected eq true(),
                SelectedLearnerSlot capture Learner.Quiz eq !"quiz",
                "quiz" has Quiz.Name eq "name",
                -"quiz" and "name"
            )
        })
        println("QUIZZES: $quizResults")
        assertEquals(2, quizResults.size)
        assertEquals(setOf("Basics", "Advanced"), quizResults.map { it["name"].asString() }.toSet())

        val selectedQuizEntity = quizResults.first { it["name"].asString() == "Basics" }["quiz"].asLong()
        assertNotNull(selectedQuizEntity)

        val lessonResults = conn.mutDb(queryOf {
            rules = listOf(
                +"selectedQuiz" put selectedQuizEntity(),
                "selectedQuiz" has Quiz.Lesson eq !"lesson",
                "lesson" has Lesson.Question eq "question",
                "lesson" has Lesson.Answer eq "answer",
                -"lesson" and "question" and "answer"
            )
        })
        println("LESSONS: $lessonResults")
        assertEquals(2, lessonResults.size)
        assertEquals(
            setOf("World", "Sekkai"),
            lessonResults.map { it["answer"].asString() }.toSet()
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