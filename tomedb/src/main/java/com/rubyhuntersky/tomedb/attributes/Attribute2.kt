package com.rubyhuntersky.tomedb.attributes

import java.util.*

interface Attribute2<T> : GroupedItem, Attribute<String> {
    val scriber: Scriber<T>
}

interface Scriber<T> {
    val emptyScript: String
    fun scribe(quant: T): String
    fun unscribe(script: String): T
}

object DateScriber : Scriber<Date> {
    override val emptyScript: String = "0"
    override fun scribe(quant: Date): String = quant.time.toString()
    override fun unscribe(script: String): Date = Date(script.toLong())
}

object StringScriber : Scriber<String> {
    override val emptyScript: String = ""
    override fun scribe(quant: String): String = quant
    override fun unscribe(script: String): String = script
}

object LongScriber : Scriber<Long> {
    override val emptyScript: String = "0"
    override fun scribe(quant: Long): String = quant.toString()
    override fun unscribe(script: String): Long = script.toLong()
}

abstract class AttributeInObject<T> : ObjectGroupedItem(), Attribute2<T> {
    override val valueType: ValueType<String> = ValueType.STRING
    override val cardinality: Cardinality = Cardinality.ONE
}