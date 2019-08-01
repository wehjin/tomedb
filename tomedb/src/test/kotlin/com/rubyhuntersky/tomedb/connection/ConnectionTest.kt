package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.ValueType
import com.rubyhuntersky.tomedb.basics.asLong
import com.rubyhuntersky.tomedb.basics.asString
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionTest {

    object Movie : Attribute.Group {

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

    @Test
    fun reconnectingLoadsLinesWithoutDuplicatingThem() {
        val ledgerWriter = TransientLedgerWriter().also {
            Client().connect(
                starter = ConnectionStarter.Attributes(listOf(Movie.Title)),
                writer = it
            ).also { connection ->
                connection.update(1, Movie.Title, Value.STRING("Return of the King"))
                connection.commit()
            }
        }

        val preReconnectLineCount = ledgerWriter.lines.size

        Client().connect(
            starter = ConnectionStarter.Data(ledgerWriter.toReader()),
            writer = ledgerWriter
        )

        val postReconnectLineCount = ledgerWriter.lines.size
        assertEquals(preReconnectLineCount, postReconnectLineCount)
    }

    @Test
    fun reconnectionLoadsDataFromLedger() {
        val ledgerWriter = TransientLedgerWriter().also {
            Client().connect(
                starter = ConnectionStarter.Attributes(listOf(Movie.Title)),
                writer = it
            ).also { connection ->
                connection.update(1, Movie.Title, Value.STRING("Return of the King"))
                connection.commit()
            }
        }
        val reconnection = Client().connect(
            starter = ConnectionStarter.Data(ledgerWriter.toReader()),
            writer = ledgerWriter
        )
        val result = reconnection.database(
            Query.Find(
                rules = listOf(
                    Rule.EVExactA(
                        "movie",
                        "title",
                        Movie.Title
                    )
                ),
                outputs = listOf("movie", "title")
            )
        )
        assertEquals(1, result.first()["movie"].asLong())
        assertEquals("Return of the King", result.first()["title"].asString())
    }

    @Test
    fun happy() {
        val connection = Client().connect(
            ConnectionStarter.Attributes(
                listOf(
                    Movie.Title,
                    Movie.Genre,
                    Movie.ReleaseYear
                )
            ),
            TransientLedgerWriter()
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
        val query = Query.Find(outputs = listOf("e"), rules = listOf(Rule.EExactA("e", Movie.Title)))
        val allMovies = db(query)
        assertEquals(3, allMovies.size)
    }
}