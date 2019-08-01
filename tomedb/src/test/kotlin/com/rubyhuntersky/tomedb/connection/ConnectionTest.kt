package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.ValueType
import com.rubyhuntersky.tomedb.basics.asLong
import com.rubyhuntersky.tomedb.basics.asString
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
        }

        object Genre : Attribute {
            override val valueType = ValueType.STRING
            override val cardinality = Cardinality.ONE
            override val description = "The genre of the movie"
        }

        object ReleaseYear : Attribute {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.ONE
            override val description = "The year the movie was released in theaters"
        }
    }

    private lateinit var dataDir: Path

    @Before
    fun setUp() {
        dataDir = TempDirFixture.initDir("connectionTest")
    }

    @Test
    fun reconnectionLoadsDataFromLedger() {
        Client().connect(dataDir, ConnectionStarter.Attributes(listOf(Movie.Title)))
            .also { connection ->
                connection.update(1, Movie.Title, Value.STRING("Return of the King"))
                connection.commit()
            }

        val reconn = Client().connect(dataDir, ConnectionStarter.None)
        val query = Query.Find(
            rules = listOf(Rule.EVExactM("movie", "title", Movie.Title)),
            outputs = listOf("movie", "title")
        )
        val result = reconn.database(query)
        assertEquals(1, result.first()["movie"].asLong())
        assertEquals("Return of the King", result.first()["title"].asString())
    }

    @Test
    fun happy() {
        val connection = Client().connect(
            dataDir,
            ConnectionStarter.Attributes(
                listOf(
                    Movie.Title,
                    Movie.Genre,
                    Movie.ReleaseYear
                )
            )
        )
        val firstMovies = listOf(
            listOf(
                Pair(Movie.Title, Value.STRING("The Goonies")),
                Pair(Cardinality.ONE, Value.STRING("action/adventure")),
                Pair(Movie.ReleaseYear, Value.LONG(1985))
            )
            , listOf(
                Pair(Movie.Title, Value.STRING("Commando")),
                Pair(Movie.Genre, Value.STRING("action/adventure")),
                Pair(Movie.ReleaseYear, Value.LONG(1985))
            )
            , listOf(
                Pair(Movie.Title, Value.STRING("Repo Man")),
                Pair(Movie.Genre, Value.STRING("punk dystopia")),
                Pair(Movie.ReleaseYear, Value.LONG(1984))
            )
        )
        connection.transactData(firstMovies)

        val db = connection.database
        val query = Query.Find(outputs = listOf("e"), rules = listOf(Rule.EExactM("e", Movie.Title)))
        val allMovies = db(query)
        assertEquals(3, allMovies.size)
    }
}