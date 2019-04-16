package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionTest {

    enum class Movie(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Title(ValueType.STRING, Cardinality.ONE, "The title of the movie"),
        Genre(ValueType.STRING, Cardinality.ONE, "The genre of the movie"),
        ReleaseYear(ValueType.LONG, Cardinality.ONE, "The year the movie was released in theaters");
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
        val result = reconnection.database[Query.Find(
            rules = listOf(Rule.EVExactA("movie", "title", Movie.Title)),
            outputs = listOf("movie", "title")
        )]
        assertEquals(1, result.first()["movie"].asLong())
        assertEquals("Return of the King", result.first()["title"].asString())
    }

    @Test
    fun happy() {
        val connection = Client().connect(
            ConnectionStarter.Attributes(
                listOf(
                    Movie.Title, Movie.Genre, Movie.ReleaseYear
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
        val allMovies = db[Query.Find(
            outputs = listOf("e"),
            rules = listOf(Rule.EExactA("e", Movie.Title))
        )]
        assertEquals(3, allMovies.size)
    }
}