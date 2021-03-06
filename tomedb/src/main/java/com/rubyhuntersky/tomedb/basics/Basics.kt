package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.ResultRow
import com.rubyhuntersky.tomedb.project
import java.util.*

fun stringToFolderName(string: String): String =
    b64Encoder.encodeToString(string.toByteArray()).replace('/', '-')

fun folderNameToString(folderName: String): String =
    String(b64Decoder.decode(folderName.replace('-', '/')))

private val b64Encoder = Base64.getEncoder()
private val b64Decoder = Base64.getDecoder()

data class Tag<T : Any>(val value: T, val keyword: Keyword)

fun <T : Any> tagOf(value: T, keyword: Keyword) = Tag(value, keyword)

data class TagList(val tags: List<Tag<*>>) : Iterable<Tag<*>> {
    override fun iterator(): Iterator<Tag<*>> = tags.iterator()
}

fun tagListOf(vararg tag: Tag<*>): TagList = TagList(tag.toList())

fun queryOf(init: Query.Find.() -> Unit): Query.Find = Query.Find(init)

fun List<Map<String, Any>>.project(slot: Query.Find.Slot): List<Any> = slot.project(this)

operator fun List<ResultRow>.get(slot: Query.Find.Slot): List<Any> =
    this.mapNotNull { it.row[slot] }
