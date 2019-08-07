package com.rubyhuntersky.tomedb.data

/**
 * A Tome is a collection of pages each relating to
 * an entity described in a topic.
 */
typealias Tome = Map<PageTitle, Page>

val Tome.pageTitles
    get() = this.keys.asSequence()

val Tome.tomeTopic: TomeTopic?
    get() = this.pageTitles.firstOrNull()?.tomeTopic

operator fun Tome.invoke(pageTitle: PageTitle): Page = this[pageTitle] ?: error("No title in tome: $pageTitle")

fun tomeOf(pages: Set<Page>): Map<PageTitle, Page> = pages.associateBy { it.pageTitle }

