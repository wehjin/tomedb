package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.basics.project
import com.rubyhuntersky.tomedb.basics.queryOf
import com.rubyhuntersky.tomedb.basics.tagListOf
import com.rubyhuntersky.tomedb.basics.tagOf
import com.rubyhuntersky.tomedb.connection.FileSession
import com.rubyhuntersky.tomedb.database.query
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

    object SelectedLearnerSlot : Query.Find.Slot {
        override val slotName: String = "selectedLearner"
    }

    @Test
    fun happy() {
        val spec: List<Attribute<*>> = listOf(*Lesson.attrs(), *Quiz.attrs(), *Learner.attrs())
        val conn = FileSession(dataDir, spec)

        val findSelectedLearners = queryOf {
            rules = listOf(
                SelectedLearnerSlot has Learner.Selected eq true,
                -SelectedLearnerSlot
            )
        }
        val tx = conn.transactor
        val selectedLearnersResult1 = tx.getDb().query(findSelectedLearners)
        println("SELECTED LEARNERS 1: ${selectedLearnersResult1.project(SelectedLearnerSlot)}")
        assertEquals(0, selectedLearnersResult1.size)

        conn.transactData(listOf(learnerData))
        val selectedLearnersResult2 = tx.getDb().query(findSelectedLearners)
        println("SELECTED LEARNERS 2: ${SelectedLearnerSlot.project(selectedLearnersResult2)}")
        assertEquals(1, selectedLearnersResult2.size)

        val quizResults = tx.getDb().query(query = queryOf {
            rules = listOf(
                SelectedLearnerSlot has Learner.Selected eq true,
                SelectedLearnerSlot has Learner.Quiz eq !"quiz",
                "quiz" has Quiz.Name eq "name",
                -"quiz" and "name"
            )
        })
        println("QUIZZES: $quizResults")
        assertEquals(2, quizResults.size)
        assertEquals(setOf("Basics", "Advanced"), quizResults.map { it["name"] as String }.toSet())

        val selectedQuizEntity =
            quizResults.first { it["name"] as String == "Basics" }["quiz"] as Long
        assertNotNull(selectedQuizEntity)

        val lessonResults = tx.getDb().query(query = queryOf {
            rules = listOf(
                +"selectedQuiz" put selectedQuizEntity,
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
            lessonResults.map { it["answer"] as String }.toSet()
        )
    }

    private val learnerData = tagListOf(
        tagOf("Default", Learner.Name.attrName),
        tagOf(true, Learner.Selected.attrName),
        tagOf(
            tagListOf(
                tagOf("Basics", Quiz.Name.attrName),
                tagOf("Life", Quiz.Publisher.attrName),
                tagOf(
                    tagListOf(
                        tagOf("Hello?", Lesson.Question.attrName),
                        tagOf("World", Lesson.Answer.attrName)
                    ), Quiz.Lesson.attrName
                ),
                tagOf(
                    tagListOf(
                        tagOf("Moshi moshi ka", Lesson.Question.attrName),
                        tagOf("Sekkai", Lesson.Answer.attrName)
                    ), Quiz.Lesson.attrName
                )
            ), Learner.Quiz.attrName
        ),
        tagOf(
            tagListOf(
                tagOf("Advanced", Quiz.Name.attrName),
                tagOf("Life", Quiz.Publisher.attrName),
                tagOf(
                    tagListOf(
                        tagOf("World?", Lesson.Question.attrName),
                        tagOf("Hello", Lesson.Answer.attrName)
                    ), Quiz.Lesson.attrName
                ),
                tagOf(
                    tagListOf(
                        tagOf("Sekkai ka", Lesson.Question.attrName),
                        tagOf("Moshi moshi", Lesson.Answer.attrName)
                    ), Quiz.Lesson.attrName
                )
            ), Learner.Quiz.attrName
        )
    )
}
