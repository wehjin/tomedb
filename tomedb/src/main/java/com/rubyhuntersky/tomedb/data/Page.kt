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

val Page.pageTitle: Title
    get() = this[pageTitleKeyword] as Title

inline operator fun <reified T : Any> Page.invoke(attr: Attribute): T = this[attr.attrName] as T

fun pageOf(title: Title, lines: List<Line<Any>>): Page {
    return lines.associate { it } + mapOf(pageTitleKeyword to title)
}

private val pageTitleKeyword = Keyword("Db.Page", "Title")



