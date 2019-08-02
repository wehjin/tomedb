package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.Query
import java.math.BigDecimal
import java.util.*

fun Value<*>?.asString(): String = (this as Value.STRING).v
fun Value<*>?.asLong(): Long = (this as Value.LONG).v

fun stringToFolderName(string: String): String = b64Encoder.encodeToString(string.toByteArray()).replace('/', '-')
fun folderNameToString(folderName: String): String = String(b64Decoder.decode(folderName.replace('-', '/')))

private val b64Encoder = Base64.getEncoder()
private val b64Decoder = Base64.getDecoder()

operator fun Boolean.invoke() = Value.BOOLEAN(this)
operator fun Long.invoke() = Value.LONG(this)
operator fun Int.invoke() = Value.LONG(this.toLong())
operator fun Keyword.invoke() = Value.ATTR(this)
operator fun String.invoke() = Value.STRING(this)
operator fun Date.invoke() = Value.INSTANT(this)
operator fun Double.invoke() = Value.DOUBLE(this)
operator fun BigDecimal.invoke() = Value.BIGDEC(this)
operator fun <T : Any> Value<T>.invoke() = Value.VALUE(AnyValue(this))
operator fun TagList.invoke() = Value.DATA(this)

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

fun queryOf(init: Query.Find2.() -> Unit): Query.Find2 = Query.Find2(init)

operator fun List<Map<String, Value<*>>>.invoke(slot: Query.Find2.Slot): List<Value<*>> = slot(this)


