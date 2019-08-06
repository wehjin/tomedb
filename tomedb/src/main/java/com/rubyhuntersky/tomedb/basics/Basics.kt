package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.ResultRow
import java.math.BigDecimal
import java.util.*

fun Value<*>?.asString(): String = (this as Value.STRING).v
fun Value<*>?.asLong(): Long = (this as Value.LONG).v

fun stringToFolderName(string: String): String = b64Encoder.encodeToString(string.toByteArray()).replace('/', '-')
fun folderNameToString(folderName: String): String = String(b64Decoder.decode(folderName.replace('-', '/')))

private val b64Encoder = Base64.getEncoder()
private val b64Decoder = Base64.getDecoder()

// TODO Remove these?
operator fun Boolean.invoke(): Value<Boolean> = Value.of(this)

operator fun Long.invoke(): Value<Long> = Value.of(this)
operator fun Int.invoke(): Value<Long> = Value.of(this.toLong())
operator fun Keyword.invoke(): Value<Keyword> = Value.of(this)
operator fun String.invoke(): Value<String> = Value.of(this)
operator fun Date.invoke(): Value<Date> = Value.of(this)
operator fun Double.invoke(): Value<Double> = Value.of(this)
operator fun BigDecimal.invoke(): Value<BigDecimal> = Value.of(this)
operator fun AnyValue.invoke(): Value<AnyValue> = Value.of(this)
operator fun <T : Any> Value<T>.invoke(): Value<AnyValue> = Value.of(AnyValue(this))
operator fun TagList.invoke(): Value<TagList> = Value.of(this)

data class AttrValue<T : Any>(val attr: Keyword, val value: T)

data class Tag<T : Any>(val value: Value<T>, val keyword: Keyword)

fun <T : Any> tagOf(value: Value<T>, keyword: Keyword) = Tag(value, keyword)
infix fun <T : Any> Value<T>.at(keyword: Keyword) = tagOf(this, keyword)
infix fun Boolean.at(keyword: Keyword) = this() at keyword
infix fun Long.at(keyword: Keyword) = this() at keyword
infix fun Int.at(keyword: Keyword) = this() at keyword
infix fun Keyword.at(keyword: Keyword) = this() at keyword
infix fun String.at(keyword: Keyword) = this() at keyword
infix fun Date.at(keyword: Keyword) = this() at keyword
infix fun Double.at(keyword: Keyword) = this() at keyword
infix fun BigDecimal.at(keyword: Keyword) = this() at keyword
infix fun TagList.at(keyword: Keyword) = this() at keyword

// TODO Remove these too?
operator fun <T : Any> Keyword.rangeTo(value: Value<T>) = tagOf(value, this)

operator fun Keyword.rangeTo(v: Boolean) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: Long) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: Int) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: Keyword) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: String) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: Date) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: Double) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: BigDecimal) = tagOf(v(), this)
operator fun Keyword.rangeTo(v: TagList) = tagOf(v(), this)

data class TagList(val tags: List<Tag<*>>) : Iterable<Tag<*>> {

    override fun iterator(): Iterator<Tag<*>> = tags.iterator()
}

fun tagListOf(vararg tag: Tag<*>) = TagList(tag.toList())

fun queryOf(init: Query.Find.() -> Unit): Query.Find = Query.Find(init)

operator fun List<Map<String, Value<*>>>.invoke(slot: Query.Find.Slot): List<Value<*>> = slot(this)
operator fun List<ResultRow>.get(slot: Query.Find.Slot): List<Value<*>> = this.mapNotNull { it.row[slot] }
