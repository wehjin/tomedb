package com.rubyhuntersky.tomedb.quizzer

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType

sealed class Learner<T : Any>(
    override val valueType: ValueType<T>,
    override val cardinality: Cardinality,
    override val description: String
) : Attribute<T> {

    override fun toString(): String = attrName.toString()

    companion object {
        fun attrs() = arrayOf(Name, Selected, Quiz)
    }

    object Name : Learner<String>(ValueType.STRING, Cardinality.ONE, "The name of the learner")
    object Selected : Learner<Boolean>(ValueType.BOOLEAN, Cardinality.ONE, "The selected learner")
    object Quiz : Learner<Long>(ValueType.LONG, Cardinality.MANY, "A quiz held by the learner")
}