package com.rubyhuntersky.tomedb.basics

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
operator fun Attr.invoke() = Value.ATTR(this)
operator fun String.invoke() = Value.STRING(this)
operator fun Date.invoke() = Value.INSTANT(this)
operator fun Double.invoke() = Value.DOUBLE(this)
operator fun BigDecimal.invoke() = Value.BIGDEC(this)
operator fun <T : Any> Value<T>.invoke() = Value.VALUE(AnyValue(this))
operator fun TagList.invoke() = Value.DATA(this)

data class Tag<T : Any>(val value: Value<T>, val attr: Attr)

fun <T : Any> tagOf(value: Value<T>, attr: Attr) = Tag(value, attr)
infix fun <T : Any> Value<T>.at(attr: Attr) = tagOf(this, attr)
infix fun Boolean.at(attr: Attr) = this() at attr
infix fun Long.at(attr: Attr) = this() at attr
infix fun Int.at(attr: Attr) = this() at attr
infix fun Attr.at(attr: Attr) = this() at attr
infix fun String.at(attr: Attr) = this() at attr
infix fun Date.at(attr: Attr) = this() at attr
infix fun Double.at(attr: Attr) = this() at attr
infix fun BigDecimal.at(attr: Attr) = this() at attr
infix fun TagList.at(attr: Attr) = this() at attr

data class TagList(val tags: List<Tag<*>>) : Iterable<Tag<*>> {

    override fun iterator(): Iterator<Tag<*>> = tags.iterator()
}

fun tagListOf(vararg tag: Tag<*>) = TagList(tag.toList())



