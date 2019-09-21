package com.rubyhuntersky.tomedb.data

/**
 * A Tome is a collection of pages each relating to
 * an entity described in a topic.
 */
@Deprecated(message = "Use Entities instead.")
data class Tome<KeyT : Any>(
    val topic: TomeTopic<KeyT>,
    val pages: Map<KeyT, Page<KeyT>>
)

val <KeyT : Any> Tome<KeyT>.size: Int
    get() = pages.size

operator fun <KeyT : Any> Tome<KeyT>.invoke(key: KeyT): Page<KeyT>? {
    return pages.values.asSequence().first { it.subject.key == key }
}

fun <KeyT : Any> tomeOf(topic: TomeTopic<KeyT>, pages: Set<Page<KeyT>>): Tome<KeyT> {
    return Tome(topic, pages.associateBy { it.key })
}
