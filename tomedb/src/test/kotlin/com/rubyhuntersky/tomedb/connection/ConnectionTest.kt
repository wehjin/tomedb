package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.file.Path

class ConnectionTest {

    object Movie {

        object Title : Attribute {
            override val valueType = ValueType.STRING
            override val cardinality = Cardinality.ONE
            override val description = "The title of the movie"
            override fun toString(): String = toKeywordString()
        }

        object Genre : Attribute {
            override val valueType = ValueType.STRING
            override val cardinality = Cardinality.ONE
            override val description = "The genre of the movie"
            override fun toString(): String = toKeywordString()
        }

        object ReleaseYear : Attribute {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.ONE
            override val description = "The year the movie was released in theaters"
            override fun toString(): String = toKeywordString()
        }
    }

    private lateinit var dataDir: Path

    @Before
    fun setUp() {
        dataDir = TempDirFixture.initDir("connectionTest")
    }

    @Test
    fun reconnectionLoadsDataFromLedger() {
        Client().connect(dataDir, listOf(Movie.Title))
            .also { connection ->
                connection.update(1, Movie.Title, Value.STRING("Return of the King"))
                connection.commit()
            }

        val reconn = Client().connect(dataDir)
        val query = Query.Find(
            rules = listOf(Rule.EntityContainsAnyValueAtAttr("movie", "title", Movie.Title)),
            outputs = listOf("movie", "title")
        )
        val result = reconn.database(query)
        assertEquals(1, result.first()["movie"].asLong())
        assertEquals("Return of the King", result.first()["title"].asString())
    }

    @Test
    fun happy() {
        val spec = listOf(Movie.Title, Movie.Genre, Movie.ReleaseYear)
        val conn = Client().connect(dataDir, spec)
        val firstMovies = listOf(
            tagListOf(
                "The Goonies" at Movie.Title,
                "action/adventure" at Cardinality.ONE,
                1985 at Movie.ReleaseYear
            ),
            tagListOf(
                "Commando" at Movie.Title,
                "action/adventure" at Movie.Genre,
                1985 at Movie.ReleaseYear
            ),
            tagListOf(
                "Repo Man" at Movie.Title,
                "punk dystopia" at Movie.Genre,
                1984 at Movie.ReleaseYear
            )
        )
        conn.transactData(firstMovies)

        val db = conn.database
        val query = Query.Find(outputs = listOf("e"), rules = listOf(Rule.EntityContainsAttr("e", Movie.Title)))
        val allMovies = db(query)
        assertEquals(3, allMovies.size)
    }
}