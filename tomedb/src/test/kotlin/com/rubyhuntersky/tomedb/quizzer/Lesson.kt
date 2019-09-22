package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

sealed class Lesson<T : Any>(
    override val valueType: ValueType<T>,
    override val cardinality: Cardinality,
    override val description: String
) : Attribute<T> {

    override fun toString(): String = attrName.toString()

    companion object {
        fun attrs() = arrayOf(Question, Answer)
    }

    object Question :
        Lesson<String>(ValueType.STRING, Cardinality.ONE, "The question of the lesson")

    object Answer : Lesson<String>(ValueType.STRING, Cardinality.ONE, "The answer of the lesson")
}