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
    val subject: PageSubject<KeyT>,
    val data: Map<Keyword, Any>
) {
    val key: KeyT get() = subject.dataKey

    operator fun get(attr: Attribute): Any? = this.data[attr.attrName]
    operator fun contains(attr: Attribute): Boolean = attr.attrName in data
    operator fun plus(line: Line<Any>): Page<KeyT> = copy(data = data + line)
    fun <T : Any> plusDefault(attr: Attribute, value: T): Page<KeyT> {
        return if (this.contains(attr)) this else this + lineOf(attr, value)
    }
}

inline operator fun <reified T : Any> Page<*>.invoke(attr: Attribute): T = this.data[attr.attrName] as T

fun <KeyT : Any> pageOf(subject: PageSubject<KeyT>, data: Map<Keyword, Any>): Page<KeyT> = Page(subject, data)
fun <KeyT : Any> pageOf(subject: PageSubject<KeyT>, lines: Set<Line<Any>>): Page<KeyT> = Page(subject, lines.associate { it })
