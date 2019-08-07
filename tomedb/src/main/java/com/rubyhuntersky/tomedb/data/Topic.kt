package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword

/**
 * A topic describes a set of entities in the database.
 */
sealed class Topic {

    /**
     * A single-entity topic.  Any tome generated from this topic will
     * contain a single page.
     */
    data class Entity(val ent: Ent) : Topic()

    /**
     * A topic composed of entities with the same trait. For example, if
     * the trait is Citizen/DateOfBirth, then the topic describes all
     * citizen entities with a date-of-birth value.
     */
    data class Trait(val attr: Keyword) : Topic() {
        constructor(attr: Attribute) : this(attr.attrName)
    }

    /**
     * A topic composed of entities with a common parent. For example, if
     * the parent is (Citizen/Country, Norway), then the topic describes
     * all citizen entities whose country is Norway.
     */
    data class Parent(val childAttr: Keyword, val parentEnt: Ent) : Topic() {
        constructor(childAttr: Attribute, parentEnt: Ent) : this(childAttr.attrName, parentEnt)
    }
}