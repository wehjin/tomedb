package com.rubyhuntersky.tomedb.data

/**
 * A Tome is a collection of pages each relating to
 * an entity described in a topic.
 */
data class Tome<KeyT : Any>(
    val topic: TomeTopic<KeyT>,
    val pages: Map<KeyT, Page<KeyT>>
) {
    val pageList: List<Page<KeyT>> by lazy { pages.values.toList() }
}

val <KeyT : Any> Tome<KeyT>.size: Int
    get() = pages.size

operator fun <KeyT : Any> Tome<KeyT>.invoke(key: KeyT): Page<KeyT>? {
    return pages.values.asSequence().first { it.subject.key == key }
}

fun <KeyT : Any> Tome<KeyT>.newPageSubject(keyValue: KeyT): PageSubject<KeyT> {
    return topic.toSubject(keyValue)
}

operator fun <KeyT : Any> Tome<KeyT>.plus(page: Page<KeyT>): Tome<KeyT> {
    return copy(pages = pages + mapOf(page.key to page))
}

operator fun <KeyT : Any> Tome<KeyT>.minus(key: KeyT): Tome<KeyT> {
    return this(key)?.key?.let { copy(pages = pages - it) } ?: this
}


fun <KeyT : Any> tomeOf(topic: TomeTopic<KeyT>, pages: Set<Page<KeyT>>): Tome<KeyT> {
    return Tome(topic, pages.associateBy { it.key })
}
