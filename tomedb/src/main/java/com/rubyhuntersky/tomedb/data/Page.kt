package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A page is a grouping of attributes and values for an
 * entity described by a topic.  The attributes and values
 * are lines in the page.  The entity and topic is the title
 * of the page.
 */
typealias Page = Map<Keyword, Any>

val Page.pageTitle: PageTitle
    get() = this[pageTitleKeyword] as PageTitle

inline operator fun <reified T : Any> Page.invoke(attr: Attribute): T = this[attr.attrName] as T

fun pageOf(pageTitle: PageTitle, lines: Set<Line<Any>>): Page {
    return lines.associate { it } + mapOf(pageTitleKeyword to pageTitle)
}

private val pageTitleKeyword = Keyword("Db.Page", "Title")



