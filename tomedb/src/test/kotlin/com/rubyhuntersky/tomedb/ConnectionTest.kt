package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionTest {

    enum class MovieAttribute(
        override val valueType: ValueType,
        override val cardinality: Cardinality,
        override val description: String
    ) : Attribute {
        Title(ValueType.STRING, Cardinality.ONE, "The title of the movie"),
        Genre(ValueType.STRING, Cardinality.ONE, "The genre of the movie"),
        ReleaseYear(ValueType.LONG, Cardinality.ONE, "The year the movie was released in theaters");
    }

    @Test
    fun happy() {
        val conn = Client().connect(TransientLedgerWriter())
        conn.transactAttributes(
            MovieAttribute.Title,
            MovieAttribute.Genre,
            MovieAttribute.ReleaseYear
        )
        val firstMovies = listOf(
            listOf(
                Pair(MovieAttribute.Title, Value.STRING("The Goonies")),
                Pair(Cardinality.ONE, Value.STRING("action/adventure")),
                Pair(MovieAttribute.ReleaseYear, Value.LONG(1985))
            )
            , listOf(
                Pair(MovieAttribute.Title, Value.STRING("Commando")),
                Pair(MovieAttribute.Genre, Value.STRING("action/adventure")),
                Pair(MovieAttribute.ReleaseYear, Value.LONG(1985))
            )
            , listOf(
                Pair(MovieAttribute.Title, Value.STRING("Repo Man")),
                Pair(MovieAttribute.Genre, Value.STRING("punk dystopia")),
                Pair(MovieAttribute.ReleaseYear, Value.LONG(1984))
            )
        )
        conn.transactData(firstMovies)

        val db = conn.database
        val allMovies = db[Query.Find(
            outputs = listOf("e"),
            rules = listOf(Rule.EinA("e", MovieAttribute.Title))
        )]
        assertEquals(3, allMovies.size)
    }
}