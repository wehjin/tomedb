package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.tagListOf
import com.rubyhuntersky.tomedb.basics.tagOf
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.getDbValue

interface Scriber<T> {
    fun scribe(quant: T): String
    fun unscribe(script: String): T
}

interface Attribute2<T> : GroupedItem {
    val description: String
    val scriber: Scriber<T>
}

interface Attribute<ValueT : Any> : GroupedItem {
    val valueType: ValueType<ValueT>
    val cardinality: Cardinality
    val description: String
}

val <ValueT : Any> Attribute<ValueT>.attrName: Keyword
    get() = toKeyword()


operator fun <ValueT : Any> Attribute<ValueT>.plus(value: ValueT) = Pair(attrName, value)

fun <ValueT : Any> Attribute<ValueT>.toSchemeData(): TagList = tagListOf(
    tagOf(attrName, Scheme.NAME.attrName),
    tagOf(valueType.keyword, Scheme.VALUETYPE.attrName),
    tagOf(cardinality.keyword, Scheme.CARDINALITY.attrName),
    tagOf(description, Scheme.DESCRIPTION.attrName)
)

inline operator fun <reified T : Any> Attribute<T>.invoke(db: Database): T? {
    return db.getDbValue(this)
}

