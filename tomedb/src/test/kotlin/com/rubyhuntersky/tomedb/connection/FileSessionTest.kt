package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.basics.tagListOf
import com.rubyhuntersky.tomedb.basics.tagOf
import com.rubyhuntersky.tomedb.database.query
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class FileSessionTest {

    object Movie {

        object Title : Attribute<String> {
            override val valueType = ValueType.STRING
            override val cardinality = Cardinality.ONE
            override val description = "The title of the movie"
            override fun toString(): String = attrName.toString()
        }

        object Genre : Attribute<String> {
            override val valueType = ValueType.STRING
            override val cardinality = Cardinality.ONE
            override val description = "The genre of the movie"
            override fun toString(): String = attrName.toString()
        }

        object ReleaseYear : Attribute<Long> {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.ONE
            override val description = "The year the movie was released in theaters"
            override fun toString(): String = attrName.toString()
        }
    }

    private lateinit var dataDir: File

    @Before
    fun setUp() {
        dataDir = TempDirFixture.initDir("connectionTest").toFile()
    }

    @Test
    fun reconnectionLoadsDataFromLedger() {
        FileSession(dataDir, listOf(Movie.Title))
            .also { connection ->
                val update = Update(1, Movie.Title, ("Return of the King"))
                connection.transactDb(setOf(update))
                connection.commit()
            }

        val conn = FileSession(dataDir, null)
        val result = conn.mutDb.query {
            rules = listOf(
                "movie" has Movie.Title eq "title",
                -"movie" and "title"
            )
        }
        assertEquals(1, result.first()["movie"] as Long)
        assertEquals("Return of the King", result.first()["title"] as String)
    }

    @Test
    fun happy() {
        val spec = listOf(Movie.Title, Movie.Genre, Movie.ReleaseYear)
        val conn = FileSession(dataDir, spec)
        val firstMovies = listOf(
            tagListOf(
                tagOf("The Goonies", Movie.Title.attrName),
                tagOf("action/adventure", Cardinality.ONE.keyword),
                tagOf(1985, Movie.ReleaseYear.attrName)
            ),
            tagListOf(
                tagOf("Commando", Movie.Title.attrName),
                tagOf("action/adventure", Movie.Genre.attrName),
                tagOf(1985, Movie.ReleaseYear.attrName)
            ),
            tagListOf(
                tagOf("Repo Man", Movie.Title.attrName),
                tagOf("punk dystopia", Movie.Genre.attrName),
                tagOf(1984, Movie.ReleaseYear.attrName)
            )
        )
        conn.transactData(firstMovies)

        val db = conn.mutDb
        val allMovies = db.query {
            rules = listOf(
                "e" has Movie.Title,
                -"e"
            )
        }
        assertEquals(3, allMovies.size)
    }
}