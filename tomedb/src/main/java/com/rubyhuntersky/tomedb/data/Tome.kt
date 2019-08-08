package com.rubyhuntersky.tomedb.data

/**
 * A Tome is a collection of pages each relating to
 * an entity described in a topic.
 */
data class Tome<KeyT : Any>(
    val topic: TomeTopic<KeyT>,
    val pages: Map<PageTitle<KeyT>, Page<KeyT>>
) {
    val pageList: List<Page<KeyT>> by lazy { pages.values.toList() }
}

val <KeyT : Any> Tome<KeyT>.size: Int
    get() = pages.size

operator fun <KeyT : Any> Tome<KeyT>.invoke(pageTitle: PageTitle<KeyT>): Page<KeyT>? = pages[pageTitle]

fun <KeyT : Any> Tome<KeyT>.newPageTitle(key: KeyT): PageTitle<KeyT> = topic.toTitle(key)

operator fun <KeyT : Any> Tome<KeyT>.plus(page: Page<KeyT>): Tome<KeyT> {
    return copy(pages = pages + mapOf(page.title to page))
}

operator fun <KeyT : Any> Tome<KeyT>.minus(page: Page<KeyT>): Tome<KeyT> {
    return copy(pages = pages - page.title)
}


fun <KeyT : Any> tomeOf(topic: TomeTopic<KeyT>, pages: Set<Page<KeyT>>): Tome<KeyT> {
    val pagesMap = pages.associateBy { it.title }
    return Tome(topic, pagesMap)
}
