package com.rubyhuntersky.tomedb

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

        override val attrName: AttrName
            get() = this.toAttrId()
    }

    @Test
    fun addSchema() {
        val conn = Client().connect("chichi")
        conn.addAttributes(
            MovieAttribute.Title,
            MovieAttribute.Genre,
            MovieAttribute.ReleaseYear
        )
    }
}