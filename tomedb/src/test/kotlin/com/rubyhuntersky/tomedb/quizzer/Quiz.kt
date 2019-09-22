package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import java.util.*

sealed class Quiz<T : Any>(
    override val valueType: ValueType<T>,
    override val cardinality: Cardinality,
    override val description: String
) : Attribute<T> {

    override fun toString(): String = attrName.toString()

    companion object {
        fun attrs() = arrayOf(Name, Publisher, Lesson, CompletedOn)
    }

    object Name : Quiz<String>(
        valueType = ValueType.STRING,
        cardinality = Cardinality.ONE,
        description = "The name of the quiz"
    )

    object Publisher : Quiz<String>(
        valueType = ValueType.STRING,
        cardinality = Cardinality.ONE,
        description = "The name of the publisher who created the quiz"
    )

    object Lesson : Quiz<Long>(
        valueType = ValueType.LONG,
        cardinality = Cardinality.MANY,
        description = "A lesson in the quiz"
    )

    object CompletedOn :
        Quiz<Date>(
            valueType = ValueType.INSTANT,
            cardinality = Cardinality.ONE,
            description = "Time the quiz was completed"
        )
}