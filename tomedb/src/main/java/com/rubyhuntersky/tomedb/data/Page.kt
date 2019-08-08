package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A page is a grouping of attributes and values for an
 * entity described by a topic.  The attributes and values
 * are lines in the page.  The entity and topic is the title
 * of the page.
 */
data class Page<KeyT : Any>(
    val title: PageTitle<KeyT>,
    val data: Map<Keyword, Any>
) {
    val key: KeyT
        get() = title.dataKey

    operator fun plus(line: Line<Any>): Page<KeyT> = copy(data = data + line)
}

inline operator fun <reified T : Any> Page<*>.invoke(attr: Attribute): T = this.data[attr.attrName] as T

fun <KeyT : Any> pageOf(title: PageTitle<KeyT>, data: Map<Keyword, Any>): Page<KeyT> = Page(title, data)
fun <KeyT : Any> pageOf(title: PageTitle<KeyT>, lines: Set<Line<Any>>): Page<KeyT> = Page(title, lines.associate { it })
